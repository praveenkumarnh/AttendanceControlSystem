/**
   Based on code by Rudy Schlaf

   This sketch uses the MFRC522 Library to use ARDUINO RFID MODULE KIT 13.56 MHZ WITH TAGS SPI (Read).

   NOTE: I call some chunks as "spi-hack", but I think it's a debt of my knowledge
*/

#include <MFRC522.h>
#include <SD.h>
#include <Ethernet.h>

#define RST_PIN 6 //MFRC522_RST_PIN
#define SS_PIN 7  //MFRC522_SS_PIN
#define SD_PIN 4  //SD_SS_PIN
#define ETHERNET_PIN 10
#define LED_PIN 5

#define LASTNAME_BLOCKNUMBER 60
#define FIRSTNAME_BLOCKNUMBER 58
#define CODE_BLOCKNUMBER 61
#define FILENAME "dtlac.csv"

byte mac[6] = {0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED};

// NOTE: The Vertx server has a static IP address,
// when we deploy it to production, we need to recompili this sketch
IPAddress server(192, 168, 1, 56); //Vertx server

IPAddress ip(192, 168, 1, 196);
IPAddress clientDns(190, 248, 0, 1);
EthernetClient client;

MFRC522 mfrc522(SS_PIN, RST_PIN); // MFRC522 instance (RFID)
MFRC522::MIFARE_Key key;

String firstname, lastname, code;

void setup()
{
  //tag::spi-hack[]
  // disable SD SPI
  pinMode(SD_PIN, OUTPUT);
  digitalWrite(SD_PIN, HIGH);

  // disable w5100 SPI
  pinMode(ETHERNET_PIN, OUTPUT);
  digitalWrite(ETHERNET_PIN, HIGH);

  // disable mfrc522 SPI
  pinMode(SS_PIN, OUTPUT);
  digitalWrite(SS_PIN, HIGH);
  //end::spi-hack[]

  //tag::boostrap[]

  //Open de serial communication
  Serial.begin(9600);
  Serial.print("\nStarting serial port...");

  while (!Serial);// Wait until the port is ready
  
  Serial.print(" [0K]");

  Serial.print("\nStarting SD-Card...");

  SD.begin(SD_PIN); // Just touch the SD port

  if (!SD.begin(SD_PIN))
  {
    Serial.print(" [FAILED]\n");
    return;
  }

  Serial.print(" [0K]");
  //end::boostrap[]

  //tag::prepare-keys[]
  // Prepare key - all keys are set to FFFFFFFFFFFFh at chip delivery from the factory.
  for (byte i = 0; i < 6; i++)
  {
    key.keyByte[i] = 0xFF;
  }
  //end::prepare-keys[]

  Serial.print("\nStarting Ethernet ... ");
  Ethernet.begin(mac, ip, clientDns);
  Serial.print(" [0K]");

  // Give the Ethernet shield a second to initialize
  delay(1000);

  Serial.print("\nScanning ...");
}

void loop()
{
  //tag::spi-hack[]
  digitalWrite(SS_PIN, LOW); // Enable MFRC522 Module communication
  mfrc522.PCD_Init();
  //end::spi-hack[]

  //tag::scan-rfid-cards[]
  if (!mfrc522.PICC_IsNewCardPresent() || !mfrc522.PICC_ReadCardSerial())
  {
    return;
  }
  //end::scan-rfid-cards[]
  
  byte firstname_blockcontent[18], lastname_blockcontent[18], code_blockcontent[18];

  Serial.print("\nCard detected...");

  firstname = "", lastname = "", code = "";

  firstname = readBlockContent(FIRSTNAME_BLOCKNUMBER, firstname_blockcontent);
  lastname = readBlockContent(LASTNAME_BLOCKNUMBER, lastname_blockcontent);
  code = readBlockContent(CODE_BLOCKNUMBER, code_blockcontent);

  Serial.print("\nSending to ");
  Serial.print(server);
  Serial.print(" ... ");

  if (client.connect(server, 8083))
  {
    String jsonData = "{\"employeeCode\":" + code + "}";

    client.println("POST /api/tracks  HTTP/1.1");
    client.println("Host: http://192.168.1.56:8083/api/tracks");
    client.println("Content-Type: application/json");
    client.println("cache-control: no-cache");
    client.println("Connection: close");
    client.print("Content-Length: ");
    client.println(jsonData.length());
    client.println();
    client.print(jsonData);

    client.flush();
    client.stop();

    Serial.print(" [0K]");
  }
  else
  {
    Serial.print(" [FAILED]");
  }

  //tag::sd-card-write[]
  Serial.print("\nWriting on SD-Card ... ");

  File dataFile = SD.open(FILENAME, FILE_WRITE);

  Serial.print("[" + firstname + ";" + lastname + ";" + code + "]");

  if (dataFile)
  {
    dataFile.println("[" + firstname + ";" + lastname + ";" + code + "]");
    dataFile.close();

    Serial.print(" [0K]\n");
  }
  else
  {
    Serial.print(" [FAILED]\n");
  }
  //end::sd-card-write[]

  Serial.print("!!Take away the RFID-card¡¡");

  byte S = LOW;

  for (byte i = 0; i < 5; i++) // 5 seconds
  {
    for (byte j = 0; j < 10; j++)
    {
      S = (S == LOW) ? HIGH : LOW;
      digitalWrite(LED_PIN, S);
      delay(100);
    }
  }
}
