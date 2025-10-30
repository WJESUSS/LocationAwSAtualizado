package com.example.locationaws;

/**
 * 🛰️ Classe SatelliteInfo
 *
 * Essa classe é um modelo (POJO) simples que representa as informações
 * de um satélite individual detectado pelo GNSS do dispositivo.
 *
 * Ela serve como estrutura de dados para armazenar:
 *  → Azimute (posição angular no plano horizontal)
 *  → Elevação (altura no céu)
 *  → SVID (identificador único do satélite)
 *  → Tipo de constelação (GPS, GLONASS, Galileo, etc.)
 *  → Se o satélite foi usado no cálculo da posição atual
 *
 * Essa classe é normalmente utilizada pela `GNSSView` para desenhar
 * os satélites na tela, ou por outras classes que processam o status GNSS.
 */
public class SatelliteInfo {

    // 🔭 Azimute: ângulo em graus indicando a direção horizontal do satélite (0° = Norte, 90° = Leste)
    final float az;

    // ⛰️ Elevação: ângulo em graus acima do horizonte (0° = horizonte, 90° = zênite)
    final float el;

    // 🆔 SVID (Satellite Vehicle ID): identificador único do satélite dentro da constelação
    final int svid;

    // 🌍 Tipo de constelação (ex: GPS, GLONASS, Galileo, BeiDou, etc.)
    // O Android fornece esses valores através de GnssStatus.getConstellationType()
    final int constellationType;

    // ✅ Indica se o satélite foi usado para calcular o "fix" (posição atual)
    final boolean usedInFix;

    /**
     * 🧩 Construtor da classe
     *
     * Recebe e armazena todas as informações do satélite.
     *
     * @param az                Azimute (graus)
     * @param el                Elevação (graus)
     * @param svid              Identificador do satélite
     * @param constellationType Tipo da constelação (GPS, GLONASS, etc.)
     * @param usedInFix         Se foi usado no cálculo da posição
     */
    SatelliteInfo(float az, float el, int svid, int constellationType, boolean usedInFix) {
        this.az = az;
        this.el = el;
        this.svid = svid;
        this.constellationType = constellationType;
        this.usedInFix = usedInFix;
    }
}
