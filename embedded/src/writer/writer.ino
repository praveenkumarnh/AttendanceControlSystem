/**
 * Based on code by Rudy Schlaf
 *
 * This sketch uses the MFRC522 Library to use ARDUINO RFID MODULE KIT 13.56 MHZ WITH TAGS SPI (Write)
 */

#include <MFRC522.h>

#define RST_PIN 6 //MFRC522_RST_PIN
#define SS_PIN 7  //MFRC522_SS_PIN
#define SD_PIN 4  //SD_SS_PIN
#define ETHERNET_PIN 10
#define LED_PIN 5

#define LASTNAME_BLOCKNUMBER 60
#define FIRSTNAME_BLOCKNUMBER 58
#define CODE_BLOCKNUMBER 61

MFRC522 mfrc522(SS_PIN, RST_PIN); // MFRC522 instance
MFRC522::MIFARE_Key key;

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
  Serial.print("\nInitializing serial port ...");
  
  while (!Serial); // Wait until the port is ready    

  Serial.print(" [OK]");

  Serial.print("\nInitializing SPI bus ...");
  SPI.begin();
  Serial.print(" [OK]");

  //end::boostrap[]

  //tag::prepare-keys[]
  // Prepare key - all keys are set to FFFFFFFFFFFFh at chip delivery from the factory.
  for (byte i = 0; i < 6; i++)
  {
    key.keyByte[i] = 0xFF;
  }
  //end::prepare-keys[]

  mfrc522.PCD_Init();
  Serial.print("]\nScanning for a RFID card (MIFARE Classic PICC) ...\n");
}

void loop()
{
  // Look for new cards
  if (!mfrc522.PICC_IsNewCardPresent() || !mfrc522.PICC_ReadCardSerial())
  {
    return;
  }
  //end::scan-rfid-cards[]

  byte firstname_blockcontent[16], lastname_blockcontent[16], code_blockcontent[16];

  MFRC522::StatusCode status;

  //tag::rdid-card-details[]
  Serial.print("\nA RFID card was detected...\n");

  MFRC522::PICC_Type piccType = mfrc522.PICC_GetType(mfrc522.uid.sak);

  //end::rdid-card-details[]

  //tag::check-compatibility[]
  if (piccType != MFRC522::PICC_TYPE_MIFARE_MINI &&
      piccType != MFRC522::PICC_TYPE_MIFARE_1K &&
      piccType != MFRC522::PICC_TYPE_MIFARE_4K)
  {
    Serial.print("\nThis program only works with MIFARE Classic cards.");
    return;
  }
  //tag::check-compatibility[]

  Serial.setTimeout(20000); // wait until 20 seconds for input from serial

  inputBlock("firstname", firstname_blockcontent);
  writeBlock(FIRSTNAME_BLOCKNUMBER, firstname_blockcontent);

  inputBlock("lastname", lastname_blockcontent);
  writeBlock(LASTNAME_BLOCKNUMBER, lastname_blockcontent);

  inputBlock("code", code_blockcontent);
  writeBlock(CODE_BLOCKNUMBER, code_blockcontent);

  mfrc522.PICC_HaltA();      // Halt PICC
  mfrc522.PCD_StopCrypto1(); // Stop encryption on PCD

  Serial.print("\n!!Take away the RFID-card¡¡");
  delay(5000);
}
