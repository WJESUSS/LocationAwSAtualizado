package com.example.locationaws;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.location.GnssStatus;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import java.util.*;

/**
 * 🌍 Classe GNSSView
 * ------------------
 * Essa classe cria uma View personalizada que desenha um radar animado.
 * O radar mostra a posição dos satélites GNSS (GPS, GLONASS, Galileo, Beidou etc.)
 * no "céu", com base em seus azimutes e elevações.
 *
 * Cada satélite é mostrado como um ponto no radar, com sua bandeira e número (SVID).
 * Os satélites usados na posição (Fix) aparecem em verde, e os não usados em cinza.
 */
public class GNSSView extends View {

    // Nome do arquivo onde preferências serão salvas
    private static final String PREFS_NAME = "GNSSViewPrefs";

    // Chaves para armazenar configurações específicas
    private static final String KEY_SELECTED_CONSTELLATIONS = "selectedConstellations"; // constelações escolhidas
    private static final String KEY_SHOW_UNUSED_SATS = "showUnusedSats"; // exibir satélites não usados

    // Lista de constelações GNSS suportadas
    private static final int[] CONSTELLATIONS = {
            GnssStatus.CONSTELLATION_GPS,     // EUA
            GnssStatus.CONSTELLATION_GALILEO, // União Europeia
            GnssStatus.CONSTELLATION_GLONASS, // Rússia
            GnssStatus.CONSTELLATION_BEIDOU   // China
    };

    // Nomes legíveis das constelações (para mostrar no diálogo de configuração)
    private static final String[] CONSTELLATION_NAMES = {
            "GPS",
            "Galileo",
            "Glonass",
            "Beidou"
    };

    // Ângulo atual do radar (para animação de varredura)
    private float sweepAngle = 0f;

    // Objetos Paint controlam cores, estilos e espessura de traços
    private final Paint paintCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintSatUsed = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintSatUnused = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintRadarSweep = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Lista com todos os satélites atualmente visíveis
    private final List<SatelliteInfo> satellites = new ArrayList<>();

    // Bandeiras associadas a cada constelação
    private final Bitmap flagUS;
    private final Bitmap flagRU;
    private final Bitmap flagEU;
    private final Bitmap flagCN;
    private final Bitmap flagUnknown;

    // Armazenamento de preferências do usuário (para lembrar seleções)
    private SharedPreferences prefs;

    // Conjuntos de constelações escolhidas e opção de exibir satélites não usados
    private Set<Integer> selectedConstellations = new HashSet<>();
    private boolean showUnusedSats = true;

    // Cor dos satélites usados
    private int colorUsedSatellites = Color.parseColor("#4CAF50");

    // Para criar o efeito do rastro (a linha girando e sumindo aos poucos)
    private Bitmap radarTrailBitmap;
    private Canvas radarTrailCanvas;

    // ---------- CONSTRUTORES ----------

    public GNSSView(Context context) {
        this(context, null);
    }

    public GNSSView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        // Lê as preferências salvas (XML ou cache)
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Permite personalizar via XML o atributo "colorUsedSatellites"
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GNSSView);
            colorUsedSatellites = a.getColor(R.styleable.GNSSView_colorUsedSatellites, colorUsedSatellites);
            a.recycle();
        }

        // Configuração dos pincéis
        paintCircle.setStyle(Paint.Style.STROKE);
        paintCircle.setColor(Color.LTGRAY);
        paintCircle.setStrokeWidth(4f);

        paintSatUsed.setStyle(Paint.Style.FILL);
        paintSatUsed.setColor(colorUsedSatellites); // verde para satélites usados

        paintSatUnused.setStyle(Paint.Style.FILL);
        paintSatUnused.setColor(Color.GRAY); // cinza para não usados

        paintText.setColor(Color.WHITE);
        paintText.setTextSize(30f);

        paintRadarSweep.setColor(Color.CYAN); // cor da linha de varredura
        paintRadarSweep.setStrokeWidth(4f);

        // Carrega bandeiras de cada constelação
        flagUS = drawableToBitmap(context, R.drawable.president_us);
        flagRU = drawableToBitmap(context, R.drawable.president_ru);
        flagEU = drawableToBitmap(context, R.drawable.president_eu);
        flagCN = drawableToBitmap(context, R.drawable.president_cn);
        flagUnknown = drawableToBitmap(context, R.drawable.president_unknown);

        // Carrega preferências do usuário (constelações selecionadas e filtros)
        loadPreferences();

        // Inicia o radar (movimento circular contínuo)
        post(radarRunnable);
    }

    // ---------- MÉTODOS DE PREFERÊNCIAS ----------


     // Carrega preferências salvas (quais constelações estão marcadas, e se deve exibir satélites não usados)

    private void loadPreferences() {
        selectedConstellations.clear();
        Set<String> savedSet = prefs.getStringSet(KEY_SELECTED_CONSTELLATIONS, null);

        // Se for a primeira execução, marca todas as constelações
        if (savedSet == null) {
            for (int c : CONSTELLATIONS) selectedConstellations.add(c);
        } else {
            for (String s : savedSet) {
                try {
                    selectedConstellations.add(Integer.parseInt(s));
                } catch (NumberFormatException ignored) {}
            }
        }
        showUnusedSats = prefs.getBoolean(KEY_SHOW_UNUSED_SATS, true);
    }


     // Salva as preferências atuais (quando o usuário altera o filtro)

    private void savePreferences() {
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> setToSave = new HashSet<>();
        for (Integer c : selectedConstellations) setToSave.add(String.valueOf(c));
        editor.putStringSet(KEY_SELECTED_CONSTELLATIONS, setToSave);
        editor.putBoolean(KEY_SHOW_UNUSED_SATS, showUnusedSats);
        editor.apply();
    }

    // ---------- MÉTODOS DE CONVERSÃO ----------


     //Converte um Drawable (imagem do recurso XML) em Bitmap (imagem manipulável)

    private Bitmap drawableToBitmap(Context context, int resId) {
        try {
            Drawable d = AppCompatResources.getDrawable(context, resId);
            if (d == null) return null;
            Bitmap bmp = Bitmap.createBitmap(
                    d.getIntrinsicWidth() > 0 ? d.getIntrinsicWidth() : 1,
                    d.getIntrinsicHeight() > 0 ? d.getIntrinsicHeight() : 1,
                    Bitmap.Config.ARGB_8888
            );
            Canvas c = new Canvas(bmp);
            d.setBounds(0, 0, c.getWidth(), c.getHeight());
            d.draw(c);
            return bmp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ---------- MÉTODOS PRINCIPAIS ----------


     //Atualiza a lista de satélites GNSS com base no status fornecido pelo GPS
     // (Esse método é chamado toda vez que o sistema recebe uma atualização do GNSS)

    public void updateSatellites(GnssStatus status) {
        synchronized (satellites) {
            satellites.clear(); // limpa lista anterior
            for (int i = 0; i < status.getSatelliteCount(); i++) {
                int constellation = status.getConstellationType(i);
                if (!selectedConstellations.contains(constellation)) continue; // ignora constelações não selecionadas

                float az = status.getAzimuthDegrees(i);  // Azimute (posição horizontal no céu)
                float el = status.getElevationDegrees(i); // Elevação (altura acima do horizonte)
                int svid = status.getSvid(i);             // ID do satélite
                boolean usedInFix = status.usedInFix(i);  // Está sendo usado no cálculo da posição?

                if (!showUnusedSats && !usedInFix) continue; // se o filtro estiver ativo, ignora não usados

                // Adiciona satélite à lista
                satellites.add(new SatelliteInfo(az, el, svid, constellation, usedInFix));
            }
        }
        // Solicita redesenho da View (para exibir os novos satélites)
        postInvalidateOnAnimation();
    }

    // ---------- DESENHO DO RADAR ----------

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        int cx = w / 2; // centro X
        int cy = h / 2; // centro Y
        int R = (int) (0.9 * Math.min(cx, cy)); // raio máximo do radar

        // Cria (ou recria) o bitmap que guarda o rastro do radar
        if (radarTrailBitmap == null || radarTrailBitmap.getWidth() != w || radarTrailBitmap.getHeight() != h) {
            radarTrailBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            radarTrailCanvas = new Canvas(radarTrailBitmap);
        }

        // Aplica uma camada transparente para criar o efeito de "desaparecimento gradual" do rastro
        radarTrailCanvas.drawColor(Color.argb(40, 10, 25, 50));

        // Calcula as coordenadas da linha de varredura com base no ângulo atual
        float sweepX = (float) (cx + R * Math.sin(Math.toRadians(sweepAngle)));
        float sweepY = (float) (cy - R * Math.cos(Math.toRadians(sweepAngle)));
        radarTrailCanvas.drawLine(cx, cy, sweepX, sweepY, paintRadarSweep);

        // Desenha o rastro animado no canvas principal
        canvas.drawBitmap(radarTrailBitmap, 0, 0, null);

        // Desenha os círculos do radar (para representar níveis de elevação)
        paintCircle.setColor(Color.LTGRAY);
        paintCircle.setStrokeWidth(4f);
        canvas.drawCircle(cx, cy, R, paintCircle);
        canvas.drawCircle(cx, cy, R * 2 / 3f, paintCircle);
        canvas.drawCircle(cx, cy, R / 3f, paintCircle);

        // Linhas cruzadas (N-S e L-O)
        paintCircle.setColor(Color.DKGRAY);
        paintCircle.setStrokeWidth(2f);
        canvas.drawLine(cx, cy - R, cx, cy + R, paintCircle);
        canvas.drawLine(cx - R, cy, cx + R, cy, paintCircle);

        // Indicador de Norte
        paintText.setColor(Color.DKGRAY);
        paintText.setTextSize(36f);
        canvas.drawText("N", cx - 12f, cy - R - 12f, paintText);

        // Desenha os satélites
        synchronized (satellites) {
            for (SatelliteInfo s : satellites) {
                drawSatellite(canvas, cx, cy, R, s);
            }
        }

        // Mostra texto com contagem de satélites
        int totalVisible = satellites.size();
        int totalUsed = 0;
        for (SatelliteInfo s : satellites) if (s.usedInFix) totalUsed++;

        paintText.setColor(Color.WHITE);
        paintText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paintText.setTextSize(36f);

        float baseY = cy - R - 60f;
        String visibleText = "Visíveis: " + totalVisible;
        String usedText = "Usados no Fix: " + totalUsed;
        float maxWidth = Math.max(paintText.measureText(visibleText), paintText.measureText(usedText));
        float startX = cx - maxWidth / 2f;

        canvas.drawText(visibleText, startX, baseY, paintText);
        canvas.drawText(usedText, startX, baseY - 44f, paintText);
        paintText.setTypeface(Typeface.DEFAULT);
    }

    // ---------- DESENHO DE CADA SATÉLITE ----------


     // Desenha um satélite individual (círculo + ID + bandeira)

    private void drawSatellite(Canvas canvas, int cx, int cy, int R, SatelliteInfo s) {
        // Converte a posição (azimute e elevação) em coordenadas x/y no radar
        // Elevação alta = próximo do centro. Baixa = mais próximo da borda.
        double rPrime = R * Math.cos(Math.toRadians(s.el));
        double x = rPrime * Math.sin(Math.toRadians(s.az));
        double y = rPrime * Math.cos(Math.toRadians(s.az));

        float drawX = (float) (cx + x);
        float drawY = (float) (cy - y);

        // Cor depende se o satélite é usado ou não
        Paint paintSat = s.usedInFix ? paintSatUsed : paintSatUnused;
        canvas.drawCircle(drawX, drawY, 12f, paintSat);

        // Desenha o número do satélite (SVID)
        paintText.setColor(Color.WHITE);
        paintText.setTextSize(28f);
        canvas.drawText(String.valueOf(s.svid), drawX + 22f, drawY + 10f, paintText);

        // Bandeira da constelação
        Bitmap flag = getFlagBitmapForConstellation(s.constellationType);
        if (flag != null) {
            int size = 55;
            Bitmap scaled = Bitmap.createScaledBitmap(flag, size, size, true);
            Bitmap circularFlag = getCircularBitmap(scaled); // recorta em formato circular
            canvas.drawBitmap(circularFlag, drawX - size - 23f, drawY - size / 2f, null);
        }
    }

    /**
     * Cria um bitmap circular a partir de uma imagem quadrada (usado nas bandeiras)
     */
    private Bitmap getCircularBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final float radius = bitmap.getWidth() / 2f;
        BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        paint.setShader(shader);
        canvas.drawCircle(radius, radius, radius, paint);
        return output;
    }

    /**
     * Retorna a bandeira correspondente à constelação GNSS
     */
    private Bitmap getFlagBitmapForConstellation(int type) {
        switch (type) {
            case GnssStatus.CONSTELLATION_GPS: return flagUS;
            case GnssStatus.CONSTELLATION_GLONASS: return flagRU;
            case GnssStatus.CONSTELLATION_GALILEO: return flagEU;
            case GnssStatus.CONSTELLATION_BEIDOU: return flagCN;
            default: return flagUnknown;
        }
    }

    // ---------- INTERAÇÃO COM O USUÁRIO ----------

    /**
     * Quando o usuário toca na tela, abre o menu de configuração
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            showConfigDialog();
            return true;
        }
        return super.onTouchEvent(event);
    }

    /**
     * Mostra o diálogo de configuração (seleção de constelações e exibição de satélites)
     */
    private void showConfigDialog() {
        Context context = getContext();
        final boolean[] checkedConstellations = new boolean[CONSTELLATIONS.length];
        for (int i = 0; i < CONSTELLATIONS.length; i++) {
            checkedConstellations[i] = selectedConstellations.contains(CONSTELLATIONS[i]);
        }
        final boolean[] checkedShowUnused = {showUnusedSats};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Configurar Visualização");

        // Caixa de seleção para cada constelação
        builder.setMultiChoiceItems(CONSTELLATION_NAMES, checkedConstellations,
                (dialog, which, isChecked) -> checkedConstellations[which] = isChecked);

        // Botão salvar configurações
        builder.setPositiveButton("Salvar", (dialog, which) -> {
            selectedConstellations.clear();
            for (int i = 0; i < CONSTELLATIONS.length; i++) {
                if (checkedConstellations[i]) selectedConstellations.add(CONSTELLATIONS[i]);
            }
            showUnusedSats = checkedShowUnused[0];
            savePreferences();
            invalidate(); // redesenha radar
        });

        builder.setNeutralButton("Cancelar", null);

        // Checkbox extra para mostrar satélites não usados
        builder.setView(createShowUnusedCheckboxView(context, checkedShowUnused));
        builder.show();
    }

    /**
     * Cria a checkbox "Mostrar satélites não usados"
     */
    private View createShowUnusedCheckboxView(Context context, boolean[] checkedShowUnused) {
        android.widget.CheckBox checkBox = new android.widget.CheckBox(context);
        checkBox.setText("Mostrar satélites não usados no Fix");
        checkBox.setChecked(checkedShowUnused[0]);
        checkBox.setPadding(50, 20, 0, 20);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> checkedShowUnused[0] = isChecked);
        return checkBox;
    }

    // ---------- CLASSE INTERNA ----------

    /**
     * Representa um satélite individual (dados GNSS simplificados)
     */
    private static class SatelliteInfo {
        final float az; // Azimute (ângulo horizontal)
        final float el; // Elevação (ângulo vertical)
        final int svid; // Número de identificação
        final int constellationType; // Tipo (GPS, GLONASS etc.)
        final boolean usedInFix; // Se está sendo usado para calcular a posição

        SatelliteInfo(float az, float el, int svid, int constellationType, boolean usedInFix) {
            this.az = az;
            this.el = el;
            this.svid = svid;
            this.constellationType = constellationType;
            this.usedInFix = usedInFix;
        }
    }

    // ---------- ANIMAÇÃO DO RADAR ----------

    /**
     * Runnable que faz o radar "girar" continuamente.
     * É executado a cada 30 milissegundos.
     */
    private final Runnable radarRunnable = new Runnable() {
        @Override
        public void run() {
            sweepAngle += 3f; // aumenta o ângulo (3° por frame)
            if (sweepAngle >= 360f) sweepAngle -= 360f; // reinicia ao completar o círculo
            invalidate(); // redesenha o radar
            postDelayed(this, 30); // chama novamente daqui a 30ms (~33fps)
        }
    };
}

//📊 Resumo geral
//Requisito	Situação	Observações
//Componente customizado	✅	Extende View
//Projeção da esfera celeste	✅	Projeção polar correta
//Topo = Norte	✅	Azimute 0° no topo
//Atualiza no GNSS	✅	Método updateSatellites() com invalidate()
//Identificação visual	✅	Cor, texto e bandeira
//Contagem de satélites	✅	Texto superior com visíveis e usados
//Diálogo de configuração	✅	Interno ao componente
//Persistência (SharedPreferences)	✅	Correta
//Atributo configurável via XML	✅