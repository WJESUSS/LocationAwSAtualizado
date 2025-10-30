package com.example.locationaws;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class FusedLocationActivity extends AppCompatActivity {

    // Cliente principal do serviço de localização do Google (Fused Location Provider)
    private FusedLocationProviderClient fusedLocationClient;

    // Callback que será chamado sempre que o GPS fornecer uma nova localização
    private LocationCallback locationCallback;

    // Objeto que configura o intervalo e prioridade das atualizações de localização
    private LocationRequest locationRequest;

    // Elementos visuais da interface (TextViews e Botões)
    private TextView txtStatus, txtDados;
    private Button btnStart, btnStop;

    // Objeto responsável por converter coordenadas em endereços legíveis
    private Geocoder geocoder;

    // Gerenciador de permissões (permite solicitar permissões em tempo de execução)
    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                // Verifica se as permissões de localização foram concedidas
                Boolean fine = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarse = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (fine || coarse) {
                    // Se ao menos uma permissão foi concedida, inicia o rastreamento
                    startLocationUpdates();
                } else {
                    // Caso contrário, avisa que foi negada
                    Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fused_location);

        // Referencia os elementos do layout XML
        txtStatus = findViewById(R.id.txtStatus);
        txtDados = findViewById(R.id.txtDados);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);

        // Inicializa o cliente de localização e o Geocoder (para converter latitude/longitude em endereço)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());

        // Cria o pedido de localização (a partir do Android 12 - API 31)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            locationRequest = new LocationRequest.Builder(5000) // intervalo de 5 segundos
                    .setMinUpdateIntervalMillis(2000) // tempo mínimo entre atualizações (2s)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY) // alta precisão (GPS)
                    .build();
        }

        // Define o callback que será chamado toda vez que uma nova localização for recebida
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    // Caso o sistema não tenha retornado nenhuma localização
                    txtStatus.setText("❌ Sem dados de localização");
                    return;
                }

                // Percorre todas as localizações disponíveis (geralmente 1 por atualização)
                for (Location location : locationResult.getLocations()) {
                    // Converte latitude e longitude em endereço (cidade, rua, etc.)
                    String endereco = getEndereco(location.getLatitude(), location.getLongitude());

                    // Monta o texto com as informações detalhadas
                    String dados = String.format(
                            "📍 Localização Atual\n\n" +
                                    "🧭 Latitude: %.6f\n" +
                                    "🧭 Longitude: %.6f\n" +
                                    "⛰️ Altitude: %.1f m\n" +
                                    "🚗 Velocidade: %.2f m/s\n" +
                                    "🎯 Precisão: ±%.2f m\n\n" +
                                    "🏠 Endereço:\n%s",
                            location.getLatitude(),
                            location.getLongitude(),
                            location.getAltitude(),
                            location.getSpeed(),
                            location.getAccuracy(),
                            endereco
                    );

                    // Atualiza o texto na tela com os dados da localização
                    txtStatus.setText("✅ Localização Ativa");
                    txtDados.setText(dados);
                }
            }
        };

        // Define as ações dos botões
        btnStart.setOnClickListener(v -> checkPermissionAndStart());
        btnStop.setOnClickListener(v -> stopLocationUpdates());
    }

    // Método que verifica as permissões e inicia o rastreamento de localização
    private void checkPermissionAndStart() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Caso a permissão ainda não tenha sido concedida, solicita ao usuário
            permissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        } else {
            // Se já tiver permissão, inicia diretamente
            startLocationUpdates();
        }
    }

    // Método responsável por começar a receber atualizações de localização
    @SuppressLint("MissingPermission") // suprime o aviso porque a permissão já foi verificada
    private void startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
        txtStatus.setText("🔄 Obtendo localização...");
        Toast.makeText(this, "Localização iniciada", Toast.LENGTH_SHORT).show();
    }

    // Método que interrompe o rastreamento de localização
    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        txtStatus.setText("⛔ Localização parada");
        txtDados.setText("Aguardando nova solicitação...");
        Toast.makeText(this, "Localização parada", Toast.LENGTH_SHORT).show();
    }

    // Método que transforma latitude/longitude em um endereço legível (rua, cidade, país, etc.)
    private String getEndereco(double latitude, double longitude) {
        try {
            // Usa o geocoder para buscar o endereço correspondente às coordenadas
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                // Monta o endereço em formato de texto
                StringBuilder sb = new StringBuilder();
                if (address.getThoroughfare() != null) sb.append(address.getThoroughfare()).append(", ");
                if (address.getSubThoroughfare() != null) sb.append(address.getSubThoroughfare()).append("\n");
                if (address.getSubLocality() != null) sb.append(address.getSubLocality()).append(", ");
                if (address.getLocality() != null) sb.append(address.getLocality()).append("\n");
                if (address.getAdminArea() != null) sb.append(address.getAdminArea()).append(" - ");
                if (address.getCountryName() != null) sb.append(address.getCountryName());

                return sb.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Endereço não encontrado";
    }
}
