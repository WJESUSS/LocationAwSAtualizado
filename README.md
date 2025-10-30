# 🛰️ LocationAwS — Aplicativo de Visualização GNSS e Localização em Tempo Real

![Android](https://img.shields.io/badge/Android-API%2034%2B-brightgreen)
![Java](https://img.shields.io/badge/Java-17%2B-orange)
![License](https://img.shields.io/badge/License-MIT-blue)
![Status](https://img.shields.io/badge/Status-Em%20Desenvolvimento-yellow)

---

## 📖 Sobre o Projeto

**LocationAwS** é um aplicativo Android para **monitoramento e visualização de informações GNSS (Global Navigation Satellite System)** em tempo real.  
Ele exibe:

* **Localização atual** (Latitude, Longitude, Altitude)
* **Número de satélites** detectados e utilizados no cálculo (FIX)
* **Intensidade do sinal (SNR)**
* **Constelações ativas** (GPS, GLONASS, Galileo, BeiDou, entre outras)

> 💡 Desenvolvido para aprendizado, pesquisa e monitoramento GNSS — ideal para estudantes e profissionais de geolocalização e engenharia de telecomunicações.

---

## 🚀 Funcionalidades Principais

| Categoria | Descrição |
| :---------- | :---------- |
| 📍 **Localização** | Latitude, longitude, altitude e precisão em tempo real. |
| 🛰️ **Satélites** | Contagem e status dos satélites detectados e utilizados no FIX. |
| 🌐 **Constelações** | Suporte e visualização de GPS, GLONASS, Galileo, BeiDou e outros. |
| 📊 **Dados Técnicos** | Exibição de ID, tipo, intensidade e status de cada satélite. |
| 🔔 **Atualização** | Dados GNSS atualizados continuamente via `GnssStatus.Callback`. |

---

## ⚙️ Tecnologias Utilizadas

| Tipo | Tecnologia | Detalhes |
| :------ | :------------ | :------- |
| 💻 **Linguagem** | Java 17+ | Linguagem principal do projeto. |
| 🧱 **Framework** | Android SDK / API 34+ | Versão mínima do SDK suportada. |
| 📍 **Localização** | `LocationManager`, `GnssStatus.Callback` | APIs nativas do Android para dados GNSS. |
| 🧩 **Interface** | XML Layouts e Custom Views (Canvas) | Visualização customizada da projeção dos satélites. |
| 🔐 **Permissões** | `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` | Permissões necessárias para acessar dados de localização. |

---

## 🧩 Estrutura do Projeto

A estrutura do projeto segue o padrão Android, com destaque para as classes de visualização e lógica GNSS:

LocationAwS/ │ 
├── app/ │ 
├── src/ │ 
│ └── main/ │ 
│ ├── java/com/example/locationaws/ │ │ 
│ ├── MainActivity.java (Ponto de entrada) │ │
│ ├── GpsViewActivity.java (Exibe a visualização GNSS) │ │ 
│ ├── GNSSView.java (Componente de visualização customizada) │ │
│ └── BaseGnssView.java │ │ 
├── res/ │ │ │ 
├── layout/ │ │
│ └── values/ │ │ 
└── AndroidManifest.xml (Declara as permissões) │ 
└── build.gradle 
└── README.md

## 🧭 Como Executar o Projeto

Siga estes passos para rodar o LocationAwS localmente:

1.  **Clone o repositório**
    ```bash
    git clone [https://github.com/WJESUSS/LocationAwS.git](https://github.com/WJESUSS/LocationAwS.git)
    cd LocationAwS
    ```
2.  **Abra o projeto no Android Studio** (Versão Iguana ou mais recente).
    * `File` → `Open` → Selecione a pasta `LocationAwS/`.
3.  **Configure o Ambiente**
    * Certifique-se de ter o **Java 17+** e o **Android SDK 34+** instalados.
4.  **Sincronize o Gradle**
    * Clique em `File` → `Sync Project with Gradle Files`.
5.  **Execute o Projeto**
    * Conecte um dispositivo Android físico (recomendado para GNSS real).
    * **Permita as permissões de localização** no dispositivo.
    * Pressione o botão **Run** (▶️) no Android Studio.

O aplicativo será iniciado e exibirá sua localização e os dados dos satélites GNSS em tempo real.

---

## 🧠 Conceitos Técnicos Envolvidos

* **GNSS (Global Navigation Satellite System):** Sistemas de posicionamento global (GPS, GLONASS, Galileo, BeiDou, etc.).
* **Fix de Localização:** O estado em que o dispositivo usa múltiplos satélites para determinar uma posição precisa.
* **Constelação:** Grupo de satélites de um mesmo sistema (ex: Constelação GPS).
* **SNR (Signal-to-Noise Ratio):** Medida da qualidade do sinal de cada satélite recebido pelo dispositivo.

---

## 💡 Melhorias Futuras Planejadas

* 🌍 Integração com Google Maps para visualização da posição no mapa.
* 🌙 Tema escuro (Dark Mode) para melhor experiência noturna.
* 💾 Salvamento de logs GNSS detalhados em Room Database.
* 📑 Exportação de relatórios de dados em CSV/JSON.
* 🧭 Suporte a sensores adicionais (bússola e giroscópio) para visualização mais precisa.

---

## 👨‍💻 Autor

| Contato | Detalhes |
| :------ | :------- |
| **Nome** | Washington Santos |
| **Email** | washquesia@gmail.com |
| **GitHub** | [@WJESUSS](https://github.com/WJESUSS) |
| **LinkedIn** | [Washington Santos](https://www.linkedin.com/in/seu-linkedin/) |

Este projeto está sob a licença **MIT**. Veja o arquivo `LICENSE` para mais detalhes.
