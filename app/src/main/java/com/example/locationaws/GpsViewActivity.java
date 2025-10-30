package com.example.locationaws;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

/**
 * 📡 Classe GpsViewActivity
 *
 * Essa Activity é responsável por exibir visualmente os satélites GNSS
 * (GPS, GLONASS, Galileo, etc.) em uma interface personalizada.
 *
 * A comunicação é feita entre:
 *  → o LocationManager (que fornece o status GNSS do Android)
 *  → a GNSSView (componente gráfico customizado que desenha os satélites na tela)
 */
public class GpsViewActivity extends AppCompatActivity {

    // Gerenciador de localização do sistema Android (controla o GPS)
    private LocationManager locationManager;

    // Componente customizado responsável por desenhar os satélites na tela
    private GNSSView gnssView;

    /**
     * 🔭 Callback que é chamado automaticamente toda vez que o status dos satélites muda.
     * O sistema Android envia as atualizações GNSS (como número de satélites, azimute e elevação)
     * e aqui repassamos essas informações para o componente gráfico GNSSView.
     */
    private final GnssStatus.Callback gnssCallback = new GnssStatus.Callback() {

        @Override
        public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
            // Executa o código na thread principal da interface (UI Thread)
            runOnUiThread(() -> {
                // Atualiza a GNSSView com o novo status de satélites
                // (a GNSSView provavelmente chamará invalidate() para redesenhar)
                if (gnssView != null) {
                    gnssView.updateSatellites(status);
                }
            });
        }
    };

    /**
     * 🧩 Método chamado quando a Activity é criada (ciclo de vida do Android).
     * Aqui configuramos a interface e inicializamos o acesso ao GPS.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cria uma nova instância da view personalizada (GNSSView)
        gnssView = new GNSSView(this);

        // Define essa view como conteúdo principal da Activity
        setContentView(gnssView);

        // Obtém o gerenciador de localização (para acessar o GPS e GNSS)
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // ⚠️ Verifica se o app tem permissão para acessar localização precisa (GPS)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Se não tiver, solicita a permissão em tempo de execução
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    2001 // Código identificador da requisição
            );
            return; // Sai do método até o usuário conceder a permissão
        }

        // ✅ Se a permissão foi concedida, registra o callback do GNSS
        // Isso inicia o monitoramento dos satélites (chamadas periódicas ao gnssCallback)
        locationManager.registerGnssStatusCallback(gnssCallback);
    }

    /**
     * 🧹 Método chamado automaticamente quando a Activity é destruída (fechada).
     * Aqui fazemos a limpeza de recursos e removemos os callbacks GNSS
     * para evitar vazamentos de memória e uso desnecessário de bateria.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            // Cancela o recebimento de atualizações GNSS
            locationManager.unregisterGnssStatusCallback(gnssCallback);
        } catch (Exception ignored) {
            // Ignora erros se o callback já tiver sido removido
        }
    }
}
