#include <Firebase_Arduino_WiFiNINA.h>

#define FIREBASE_HOST "careplant-5c9eb-default-rtdb.firebaseio.com"
#define FIREBASE_AUTH "ystThqUUAtoM4Xj4oZn3NicjGQ0E89LvKx8vpcxY"
#define WIFI_SSID "Phone?"
#define WIFI_PASSWORD "crossword"
//#define WIFI_SSID "Lux Speed - IoT"
//#define WIFI_PASSWORD "SzKWMw9WS2vU"
//#define WIFI_SSID "erchealth00"
//#define WIFI_PASSWORD "Archie11"

FirebaseData firebaseData;

int i = 1;

void setup()
{
  Serial.begin(9600);
  Serial.print("Connecting to WiFi...");
  int status = WL_IDLE_STATUS;
  while (status != WL_CONNECTED) 
  {
    status = WiFi.begin(WIFI_SSID, WIFI_PASSWORD); //inputs wifi ssid and password to connect
    Serial.print(".");
    delay(300);
  }
  Serial.print(" IP: ");
  Serial.println(WiFi.localIP()); //prints ip address
  Serial.println();

  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH, WIFI_SSID, WIFI_PASSWORD); //connects to firebase
  Firebase.reconnectWiFi(true); //tries to reconnect if failed

  pinMode(A1, OUTPUT); //sets pin A1 to water pump output
  pinMode(A0, INPUT); //sets pin A0 to soil moisture sensor input

  digitalWrite(A1, HIGH);
}


void loop()
{
  int val = 0;

  if (Firebase.getInt(firebaseData, "/WP")) //if we are able to get an integer from database, then read the payload value
  {
    if (firebaseData.dataType() == "int") //makes sure payload value returned from server is integer and prevents you from getting garbage data
    {
      val = firebaseData.intData(); //sets variable "val" to the recieved payload value
      Serial.println(val); //prints the payload value
    }
  } 
  else //failed, then print out the error detail
  {
    Serial.println(firebaseData.errorReason());
  }

//////////

  if (val == 1) //if app button is clicked, turn on pump for _ seconds then change database value back to off
  {
    digitalWrite(A1, LOW);
    delay(500);
    digitalWrite(A1, HIGH);
    Firebase.setFloat(firebaseData, "/WP", 0);
  }
  else if (val == 0) //otherwise keep pump off
  {
    digitalWrite(A1, HIGH);
  }

//////////

  // float WAAM = ((2.48 / analogRead(A0)) - 0.72);
  float WAAM = (analogRead(A0));

  //Firebase.setTimestamp(firebaseData, "/Time");

  //firebaseData.payload()
  String x = "/Data/" + String(i);
  String y = x + "/time";

  Firebase.setFloat(firebaseData, (x + "/ML"), WAAM); // Send data to Firebase with specific path
  Firebase.setTimestamp(firebaseData, y);

  Serial.print("MOISTURE LEVEL: "); //prints "MOISTURE LEVEL: "
  Serial.print(WAAM); //prints moisture level
  Serial.println();

  i = i + 1;
//////////

  // if (WAAM < 300) //if water amount is below 300, turns on pump
  // {
  //   digitalWrite(A1, LOW);
  //   delay(500);
  //   digitalWrite(A1, HIGH);
  // }
  // else //if water amount is above 300, turns off pump
  // {
  //   digitalWrite(A1, HIGH);
  // }

//////////

  delay(2000);
  Serial.println(); //prints new line for spacing
}