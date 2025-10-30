package com.example.locationaws;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class CelestialSphereActivity extends AppCompatActivity {
    private static final int REQUEST_FINE_LOCATION = 1234;

    private GNSSView gnssView;
    private GnssStatus.Callback gnssStatusCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gnssView = new GNSSView(this);
        setContentView(gnssView);

        gnssStatusCallback = new GnssStatus.Callback() {
            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                gnssView.updateSatellites(status);
            }
        };

        checkPermissionsAndRegister();
    }

    private void checkPermissionsAndRegister() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
            } else {
                registerGnssCallback();
            }
        } else {
            // Permissão automática em versões antigas
            registerGnssCallback();
        }
    }

    private void registerGnssCallback() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permissão de localização não concedida", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean registered = locationManager.registerGnssStatusCallback(gnssStatusCallback);
        if (!registered) {
            Toast.makeText(this, "Falha ao registrar callback GNSS", Toast.LENGTH_SHORT).show();
        }
    }

    private void unregisterGnssCallback() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.unregisterGnssStatusCallback(gnssStatusCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissionsAndRegister();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterGnssCallback();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                registerGnssCallback();
            } else {
                Toast.makeText(this, "Permissão de localização é necessária para o funcionamento do app", Toast.LENGTH_LONG).show();
            }
        }
    }
}
