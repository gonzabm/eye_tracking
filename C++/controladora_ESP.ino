#include "WiFi.h"
#include "ESPAsyncWebServer.h"

#define NUM_RELAYS 3    // Set number of relays
int relayGPIOs[NUM_RELAYS]    = {27, 26, 25};
String relayNames[NUM_RELAYS] = { "Frenos", "Motor Izquierdo", "Motor Derecho"};
String relayIds[NUM_RELAYS]   = { "brakes", "lMotor", "rMotor"};


// Create AsyncWebServer object on port 80
AsyncWebServer server(80);

const char html[] PROGMEM = R"rawliteral(
<!DOCTYPE HTML><html>
<head>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <style>
    html {font-family: Arial; display: inline-block; text-align: center;}
    h2 {font-size: 3.0rem;}
    p {font-size: 3.0rem;}
    body {max-width: 600px; margin:0px auto; padding-bottom: 25px;}
    .switch {position: relative; display: inline-block; width: 120px; height: 68px} 
    .switch input {display: none}
    .slider {position: absolute; top: 0; left: 0; right: 0; bottom: 0; background-color: #ccc; border-radius: 34px}
    .slider:before {position: absolute; content: ""; height: 52px; width: 52px; left: 8px; bottom: 8px; background-color: #fff; -webkit-transition: .4s; transition: .4s; border-radius: 68px}
    input:checked+.slider {background-color: #2196F3}
    input:checked+.slider:before {-webkit-transform: translateX(52px); -ms-transform: translateX(52px); transform: translateX(52px)}
  </style>
</head>
<body>
  <h2>Control de silla de ruedas</h2>
  %BUTTONPLACEHOLDER%
<script>
function toggleCheckbox(element) {
    var xhr = new XMLHttpRequest();
    xhr.open("GET", "/update?device=" + element.id + "&state=" + (element.checked ? "1" : "0"), true);
    xhr.send();
}
</script>
</body>
</html>
)rawliteral";

// Buttons are made here and replaced in the webpage
// Because we need to check if the relay is set to ON or OFF
String processor(const String& var){
  if(var == "BUTTONPLACEHOLDER"){
    String buttons ="";
    for(int i=0; i < NUM_RELAYS; i++){
      String relayState = digitalRead(relayGPIOs[i]) ? "" : "checked";
      buttons +=
        "<h4>" + relayNames[i] + "</h4>"
      + "<label class=\"switch\">"
      +   "<input type=\"checkbox\" onchange=\"toggleCheckbox(this)\" id=\"" + relayIds[i] + "\" " + relayState + ">"
      +   "<span class=\"slider\"></span>"
      + "</label>";
    }
    return buttons;
  }
  return String();
}

void setup() {
  Serial.begin(115200);   // Serial port for debugging purposes

  // Set all relays to off when the program starts - Relay is Normally Open, so the relay is off when you set the relay to HIGH
  for(int i = 0; i < NUM_RELAYS; i++){
    pinMode(relayGPIOs[i], OUTPUT);
    digitalWrite(relayGPIOs[i], HIGH);
  }
  
  // Connect to Wi-Fi
  WiFi.begin("POCO X5 5G", "m50c09f2");
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Buscando red WiFi");
  }
  Serial.println(WiFi.localIP());     // Print ESP32 Local IP Address

  // Route for root / web page
  server.on("/", HTTP_GET, [](AsyncWebServerRequest *request){
    request->send_P(200, "text/html", html, processor);
  });

  // http://<IP del ESP32>:80/update?device=<param1>&state=<param2>
  server.on("/update", HTTP_GET, [] (AsyncWebServerRequest *request) {
    String param1, param2;

    if (request->hasParam("device") && request->hasParam("state")) {

      param1 = request->getParam("device")->value();
      param2 = request->getParam("state")->value();

      int relay;
      if (param1.equals("brakes"))
        relay = 0;
      else if (param1.equals("lMotor"))
        relay = 1;
      else if (param1.equals("rMotor"))
        relay = 2;

      boolean state = !param2.toInt();   // Negated because relay is normally open

      digitalWrite(relayGPIOs[relay], state);
      Serial.println(param1 + " is " + (state ? "off" : "on"));
    }
    else {
      Serial.println("No message sent");
    }
    request->send(200, "text/plain", "OK");
  });
  server.begin();
}
  
void loop() {
}