package com.example.locationaws;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 🚀 Classe MainActivity
 *
 * Essa é a tela principal do aplicativo.
 * Ela apresenta três botões, cada um responsável por abrir uma atividade diferente:
 *
 * 1️⃣ FusedLocationActivity → Localização via API do Google (mais precisa e moderna)
 * 2️⃣ GpsLocationActivity → Exibe dados brutos do GPS e satélites (texto)
 * 3️⃣ GpsViewActivity → Mostra a posição dos satélites em uma visualização gráfica (GNSSView)
 */
public class MainActivity extends AppCompatActivity {

    // Declaração dos botões da interface
    Button btnApiLoc, btnGnssLoc, btnGnssPlot;

    /**
     * Método principal de inicialização da Activity.
     * É chamado automaticamente quando a tela é criada.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Define o layout XML da tela principal

        // 🔘 Associa cada botão com seu ID no layout XML
        btnApiLoc = findViewById(R.id.btnApiLoc);   // Botão para API de localização (FusedLocationProvider)
        btnGnssLoc = findViewById(R.id.btnGnssLoc); // Botão para localização via GNSS (texto)
        btnGnssPlot = findViewById(R.id.btnGnssPlot); // Botão para visualização GNSS (gráfica)

        // 📍 Botão: abre a tela da API de Localização (Fused Location Provider Client)
        btnApiLoc.setOnClickListener(v ->
                startActivity(new Intent(this, FusedLocationActivity.class))
        );

        // 🛰️ Botão: abre a tela com dados de satélites em texto (GNSS + localização bruta)
        btnGnssLoc.setOnClickListener(v ->
                startActivity(new Intent(this, GpsLocationActivity.class))
        );

        // 🌌 Botão: abre a visualização gráfica dos satélites (GNSSView)
        btnGnssPlot.setOnClickListener(v ->
                startActivity(new Intent(this, GpsViewActivity.class))
        );
    }
}
