package com.example.locationaws;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class GpsLocationActivity extends AppCompatActivity {

    private LocationManager locationManager;
    private TextView txtInfo;
    private ScrollView scrollView;

    private final GnssStatus.Callback gnssCallback = new GnssStatus.Callback() {
        @Override
        public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
            StringBuilder sb = new StringBuilder();
            sb.append("Satélites detectados: ").append(status.getSatelliteCount()).append("\n\n");
            for (int i = 0; i < status.getSatelliteCount(); i++) {
                sb.append("Sat #").append(i + 1)
                        .append(": Az: ").append((int) status.getAzimuthDegrees(i))
                        .append("° | El: ").append((int) status.getElevationDegrees(i))
                        .append("° | Usado no Fix: ").append(status.usedInFix(i) ? "Sim" : "Não")
                        .append("\n");
            }
            txtInfo.setText(sb.toString());

            // Scroll para o fim automaticamente
            scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
        }
    };

    private final LocationListener locationListener = location -> {
        String loc = String.format(
                "\n📍 Localização:\nLatitude: %.6f\nLongitude: %.6f\nAltitude: %.1f m",
                location.getLatitude(), location.getLongitude(), location.getAltitude());
        txtInfo.append(loc);
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_location);

        scrollView = findViewById(R.id.scrollViewInfo);
        txtInfo = findViewById(R.id.txtInfo);

        // 🔹 Ajusta tamanho da letra menor para caber todos os satélites
        txtInfo.setTextSize(14f);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        locationManager.registerGnssStatusCallback(gnssCallback);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, locationListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
        locationManager.unregisterGnssStatusCallback(gnssCallback);
    }
}
