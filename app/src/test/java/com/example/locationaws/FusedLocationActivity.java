package com.example.locationaws;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FusedLocationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Teste: Tela básica apenas para ver se abre
        TextView tv = new TextView(this);
        tv.setText("API de Localização funcionando!");
        tv.setTextSize(24);
        tv.setPadding(40, 100, 40, 40);
        setContentView(tv);
    }
}
