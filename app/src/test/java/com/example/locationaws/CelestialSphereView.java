package com.example.locationaws;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.GnssStatus;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CelestialSphereView extends View {

    private final Paint paintCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintSatUsed = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintSatNotUsed = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintFixText = new Paint(Paint.ANTI_ALIAS_FLAG);

    private List<SatelliteInfo> satellites = new ArrayList<>();

    private Set<Integer> enabledConstellations = new HashSet<>();
    private boolean showNonFix = true;

    private int visibleCount = 0;
    private int usedCount = 0;

    public CelestialSphereView(Context context) {
        super(context);
        init();
    }

    public CelestialSphereView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paintCircle.setStyle(Paint.Style.STROKE);
        paintCircle.setColor(Color.LTGRAY);
        paintCircle.setStrokeWidth(4f);

        paintSatUsed.setStyle(Paint.Style.FILL);
        paintSatUsed.setColor(Color.GREEN);

        paintSatNotUsed.setStyle(Paint.Style.FILL);
        paintSatNotUsed.setColor(Color.RED);

        paintText.setColor(Color.WHITE);
        paintText.setTextSize(28f);

        paintFixText.setColor(Color.YELLOW);
        paintFixText.setTextSize(36f);

        // Enable the 4 constellations by default
        enabledConstellations.add(GnssStatus.CONSTELLATION_GPS);
        enabledConstellations.add(GnssStatus.CONSTELLATION_GLONASS);
        enabledConstellations.add(GnssStatus.CONSTELLATION_GALILEO);
        enabledConstellations.add(GnssStatus.CONSTELLATION_BEIDOU);
    }

    public void updateSatellites(GnssStatus status) {
        List<SatelliteInfo> updated = new ArrayList<>();
        int visible = 0;
        int used = 0;

        for (int i = 0; i < status.getSatelliteCount(); i++) {
            int constType = status.getConstellationType(i);
            if (!enabledConstellations.contains(constType)) continue;

            int svid = status.getSvid(i);
            float az = status.getAzimuthDegrees(i);
            float el = status.getElevationDegrees(i);
            boolean usedInFix = status.usedInFix(i);

            if (!showNonFix && !usedInFix) {
                continue; // skip satellites not used in fix if option disabled
            }

            updated.add(new SatelliteInfo(az, el, svid, constType, usedInFix));
            visible++;
            if (usedInFix) used++;
        }

        synchronized (this) {
            satellites = updated;
            visibleCount = visible;
            usedCount = used;
        }

        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();

        int cx = w / 2;
        int cy = h / 2;

        int R = (int)(Math.min(cx, cy) * 0.9f);

        // Fundo azul escuro
        canvas.drawColor(Color.rgb(10, 24, 48));

        // Desenha círculos de elevação (90°, 60°, 30°, 0°)
        paintCircle.setColor(Color.LTGRAY);
        paintCircle.setStrokeWidth(3f);
        canvas.drawCircle(cx, cy, R, paintCircle);
        canvas.drawCircle(cx, cy, (int)(R*2f/3f), paintCircle);
        canvas.drawCircle(cx, cy, (int)(R/3f), paintCircle);

        // Norte fixo no topo
        paintText.setColor(Color.WHITE);
        paintText.setTextSize(36f);
        canvas.drawText("N", cx - 12, cy - R + 40, paintText);

        // Linhas norte-sul e leste-oeste fixas
        paintCircle.setColor(Color.GRAY);
        paintCircle.setStrokeWidth(2f);
        canvas.drawLine(cx, cy - R, cx, cy + R, paintCircle);
        canvas.drawLine(cx - R, cy, cx + R, cy, paintCircle);

        // Desenha satélites
        synchronized (this) {
            for (SatelliteInfo s : satellites) {
                drawSatellite(canvas, cx, cy, R, s);
            }
        }

        // Texto com número de satélites
        paintFixText.setColor(Color.WHITE);
        canvas.drawText("Visíveis: " + visibleCount, 20, h - 80, paintFixText);
        canvas.drawText("Usados no Fix: " + usedCount, 20, h - 30, paintFixText);
    }

    private void drawSatellite(Canvas canvas, int cx, int cy, int R, SatelliteInfo s) {
        // Converte elevação e azimute para coordenadas 2D na esfera celeste
        // Elevação 90° é centro, 0° é borda
        double r = R * (1 - s.elevation / 90.0);
        double azRad = Math.toRadians(s.azimuth);

        float x = (float) (cx + r * Math.sin(azRad));
        float y = (float) (cy - r * Math.cos(azRad));

        // Desenha satélite: círculo verde se usado no fix, vermelho se não usado
        Paint paint = s.usedInFix ? paintSatUsed : paintSatNotUsed;

        // Usar forma diferente para usado no fix: círculo preenchido, não usado triângulo
        if (s.usedInFix) {
            canvas.drawCircle(x, y, 20, paint);
        } else {
            // Triângulo apontando para cima
            float halfSize = 20;
            float[] pts = {
                    x, y - halfSize,
                    x - halfSize, y + halfSize,
                    x + halfSize, y + halfSize
            };
            canvas.drawPath(makeTrianglePath(pts), paint);
        }

        // Texto identificador e constelação ao lado
        paintText.setColor(Color.WHITE);
        paintText.setTextSize(26f);

        String constAbbr = constellationAbbreviation(s.constellationType);
        String text = constAbbr + "-" + s.svid;

        canvas.drawText(text, x + 25, y + 10, paintText);
    }

    private android.graphics.Path makeTrianglePath(float[] pts) {
        android.graphics.Path path = new android.graphics.Path();
        path.moveTo(pts[0], pts[1]);
        path.lineTo(pts[2], pts[3]);
        path.lineTo(pts[4], pts[5]);
        path.close();
        return path;
    }

    private String constellationAbbreviation(int constellationType) {
        switch (constellationType) {
            case GnssStatus.CONSTELLATION_GPS: return "GPS";
            case GnssStatus.CONSTELLATION_GLONASS: return "GLO";
            case GnssStatus.CONSTELLATION_GALILEO: return "GAL";
            case GnssStatus.CONSTELLATION_BEIDOU: return "BDS";
            default: return "UNK";
        }
    }

    // Método para mostrar diálogo de configurações das constelações e fix
    public void showConfigDialog() {
        final String[] constNames = {"GPS", "Glonass", "Galileo", "Beidou"};
        final int[] constTypes = {
                GnssStatus.CONSTELLATION_GPS,
                GnssStatus.CONSTELLATION_GLONASS,
                GnssStatus.CONSTELLATION_GALILEO,
                GnssStatus.CONSTELLATION_BEIDOU
        };

        final boolean[] checkedConstellations = new boolean[constNames.length];
        for (int i = 0; i < constNames.length; i++) {
            checkedConstellations[i] = enabledConstellations.contains(constTypes[i]);
        }
        final boolean[] checkedShowNonFix = {showNonFix};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Configurações de Satélites");

        // MultiChoice para constelações
        builder.setMultiChoiceItems(constNames, checkedConstellations, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int index, boolean checked) {
                checkedConstellations[index] = checked;
            }
        });

        // Checkbox para mostrar satélites não usados no fix
        builder.setNeutralButton(checkedShowNonFix[0] ? "Ocultar Sat. Não Fix" : "Mostrar Sat. Não Fix", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                checkedShowNonFix[0] = !checkedShowNonFix[0];
                // Atualiza botão para próximo clique
                dialogInterface.dismiss();
                showConfigDialog(); // Reabre diálogo para atualizar botão (simples, pode ser melhorado)
            }
        });

        builder.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                enabledConstellations.clear();
                for (int i = 0; i < constTypes.length; i++) {
                    if (checkedConstellations[i]) {
                        enabledConstellations.add(constTypes[i]);
                    }
                }
                showNonFix = checkedShowNonFix[0];
                invalidate();
            }
        });

        builder.setNegativeButton("Cancelar", null);

        builder.show();
    }

    // Classe interna para guardar dados dos satélites
    private static class SatelliteInfo {
        float azimuth;
        float elevation;
        int svid;
        int constellationType;
        boolean usedInFix;

        SatelliteInfo(float az, float el, int svid, int constellationType, boolean usedInFix) {
            this.azimuth = az;
            this.elevation = el;
            this.svid = svid;
            this.constellationType = constellationType;
            this.usedInFix = usedInFix;
        }
    }

    // Captura toque para mostrar diálogo
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            showConfigDialog();
            return true;
        }
        return super.onTouchEvent(event);
    }
}
