/**
 * Based on code by Rudy Schlaf
 *
 * This sketch uses the MFRC522 Library to use ARDUINO RFID MODULE KIT 13.56 MHZ WITH TAGS SPI (Read).
 *
 * @NOTE We dont need the wire library, but if we remove it, strangely, the MFRC522 stops working. I
 * call it "hack", but I think it's a debt of my knowledge
 */

#include <SPI.h>
#include <MFRC522.h>
#include "Wire.h"
#include <SD.h>

#define RST_PIN 6 //MFRC522_RST_PIN
#define SS_PIN 7  //MFRC522_SS_PIN
#define SD_PIN 4  //SD_SS_PIN
#define ETHERNET_PIN 10

#define LASTNAME_BLOCKNUMBER 60
#define FIRSTNAME_BLOCKNUMBER 58
#define CODE_BLOCKNUMBER 61
#define FILENAME "dtlac.csv"

MFRC522 mfrc522(SS_PIN, RST_PIN); // MFRC522 instance (RFID)
MFRC522::MIFARE_Key key;

String firstname, lastname, code;

void setup()
{
  //tag::boostrap[]

  //Open de serial communication
  Serial.begin(9600);
  Serial.print("\nInitializing serial port...");

  while (!Serial)
    ; // Wait until the port is ready

  Serial.print(" [0K]");

  //tag::spi-hack[]
  Serial.print("\nSeting MFRC522 PIN on LOW ...");
  // The SS PIN for the MFRC522 need to init LOW, to prevent SPI protocols conflicts
  digitalWrite(SS_PIN, LOW);
  //end::spi-hack[]

  Serial.print("\nInitializing SPI bus...");
  SPI.begin();
  Serial.print(" [0K]");

  Serial.print("\nInitializing SD-Card...");

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

  Wire.begin();
  Serial.print("\nScanning for a RFID card (MIFARE Classic PICC) ...");
}

void loop()
{
  //tag::spi-hack[]
  digitalWrite(SS_PIN, LOW);
  mfrc522.PCD_Init();
  //end::spi-hack[]

  //tag::scan-rfid-cards[]
  if (!mfrc522.PICC_IsNewCardPresent() || !mfrc522.PICC_ReadCardSerial())
  {
    return;
  }
  //end::scan-rfid-cards[]

  byte block;
  byte blockcontent[18];
  byte length;
  MFRC522::StatusCode status;

  Serial.print(F("\n------------------------------------------------------"));
  Serial.print(F("\nA RFID card was detected...\n"));

  mfrc522.PICC_DumpDetailsToSerial(&(mfrc522.uid)); // Dump some details about the RFID card

  digitalWrite(SS_PIN, LOW);  // We got UID now set pin to LOW in order to communicate with SD Module
  digitalWrite(SD_PIN, HIGH); // Enable SD Module communication

  firstname = "", lastname = "", code = ""; //Clean

  if (readBlockContent(FIRSTNAME_BLOCKNUMBER, blockcontent, firstname) == 1)
  {

    if (readBlockContent(LASTNAME_BLOCKNUMBER, blockcontent, lastname) == 1)
    {

      readBlockContent(CODE_BLOCKNUMBER, blockcontent, code);
    }
  }

  mfrc522.PICC_HaltA();
  mfrc522.PCD_StopCrypto1();

  //tag::sd-card-write[]
  Serial.print("\nWriting on SD-Card ... ");

  File dataFile = SD.open(FILENAME, FILE_WRITE);

  if (dataFile)
  {
    Serial.print("[" + firstname + ";" + lastname + ";" + code + "]");
    dataFile.println(firstname + ";" + lastname + ";" + code);
    dataFile.close();

    Serial.print(" [0K]\n");
  }
  else
  {
    Serial.print(" [FAILED]\n");
  }
  //end::sd-card-write[]

  Serial.print(F("\n**** Take away the RFID-card ****"));
  delay(5000); //Some delay to be sure that the current RFID-card was move away
}
