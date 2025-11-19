#include <WiFi.h>
#include <WebServer.h>
#include <ArduinoJson.h>

WebServer server(80);

// Estrutura para guardar dados de cada veículo
struct Veiculo {
  String id;
  float latitude;
  float longitude;
  unsigned long ultimoEnvio; // timestamp em ms
};

#define MAX_VEICULOS 20
Veiculo veiculos[MAX_VEICULOS];
int totalVeiculos = 0;

// Configuração WiFi
const char* ssid = "SEU_SSID";
const char* senha = "SUA_SENHA";

// Função para registrar/atualizar veículo
void atualizarVeiculo(String id, float lat, float lon) {
  unsigned long agora = millis();
  
  // Verifica se já existe
  for (int i = 0; i < totalVeiculos; i++) {
    if (veiculos[i].id == id) {
      veiculos[i].latitude = lat;
      veiculos[i].longitude = lon;
      veiculos[i].ultimoEnvio = agora;
      return;
    }
  }

  // Se não existe, adiciona
  if (totalVeiculos < MAX_VEICULOS) {
    veiculos[totalVeiculos].id = id;
    veiculos[totalVeiculos].latitude = lat;
    veiculos[totalVeiculos].longitude = lon;
    veiculos[totalVeiculos].ultimoEnvio = agora;
    totalVeiculos++;
  }
}

// Remove veículos inativos
void limparInativos() {
  unsigned long agora = millis();
  for (int i = 0; i < totalVeiculos; i++) {
    if (agora - veiculos[i].ultimoEnvio > 30000) { // 30s sem atualizar
      // Remove deslocando array
      for (int j = i; j < totalVeiculos - 1; j++) {
        veiculos[j] = veiculos[j + 1];
      }
      totalVeiculos--;
      i--;
    }
  }
}

// Endpoint para receber dados do site
void handleGPS() {
  if (server.hasArg("plain") == false) {
    server.send(400, "text/plain", "Bad Request");
    return;
  }

  String body = server.arg("plain");
  StaticJsonDocument<200> doc;
  DeserializationError error = deserializeJson(doc, body);

  if (error) {
    server.send(400, "text/plain", "JSON inválido");
    return;
  }

  String id = doc["id"]; // identificador do veículo
  float lat = doc["latitude"];
  float lon = doc["longitude"];

  atualizarVeiculo(id, lat, lon);

  server.send(200, "text/plain", "Localização recebida");
}

// Exemplo de cálculo de tempo do semáforo
int calcularTempoVerde() {
  limparInativos();
  int n = totalVeiculos;

  if (n == 0) return 0;
  else if (n < 5) return 5;
  else if (n < 15) return 10;
  else return 20;
}

void setup() {
  Serial.begin(115200);
  WiFi.begin(ssid, senha);

  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Conectando ao WiFi...");
  }
  Serial.println("Conectado!");
  Serial.println(WiFi.localIP());

  server.on("/gps", HTTP_POST, handleGPS);
  server.begin();
}

void loop() {
  server.handleClient();

  // Exemplo: mostra veículos ativos
  static unsigned long ultimoLog = 0;
  if (millis() - ultimoLog > 5000) {
    ultimoLog = millis();
    Serial.printf("Veículos ativos: %d\n", totalVeiculos);
    Serial.printf("Tempo verde calculado: %d segundos\n", calcularTempoVerde());
  }
}
