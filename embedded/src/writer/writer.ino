/**
 * Based on code by Rudy Schlaf 
 * 
 * This sketch uses the MFRC522 Library to use ARDUINO RFID MODULE KIT 13.56 MHZ WITH TAGS SPI W/R
 */
 
#include <SPI.h>
#include <MFRC522.h>

#define RST_PIN 6
#define SS_PIN 7

#define LASTNAME_BLOCKNUMBER 60
#define FIRSTNAME_BLOCKNUMBER 58
#define CODE_BLOCKNUMBER 61

MFRC522 mfrc522(SS_PIN, RST_PIN); // MFRC522 instance
MFRC522::MIFARE_Key key;

void setup()
{
  //tag::boostrap[]

  //Open de serial communication
  Serial.begin(9600);
  Serial.print("\nInitializing serial port ...");
  while (!Serial); // Wait until the port is ready

  Serial.print(" [OK]");

  Serial.print("\nInitializing SPI bus ...");
  SPI.begin();
  Serial.print(" [OK]");

  Serial.print("\nInitializing MFRC522 ...");
  mfrc522.PCD_Init();
  Serial.print(" [OK]");
  //end::boostrap[]

  //tag::prepare-keys[]
  // Prepare key - all keys are set to FFFFFFFFFFFFh at chip delivery from the factory.
  for (byte i = 0; i < 6; i++) {
    key.keyByte[i] = 0xFF;
  }
  //20 20  20 20 20 20
  //end::prepare-keys[]

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

  byte blockcontent[16];
  byte readbackblock[18];  
  MFRC522::StatusCode status;
  
  //tag::rdid-card-details[]
  Serial.print("\nA RFID card was detected...\n");
  Serial.print("Card UID: ");

  dumpBytetoArray(mfrc522.uid.uidByte, mfrc522.uid.size);

  Serial.print("\nPICC type: ");
  MFRC522::PICC_Type piccType = mfrc522.PICC_GetType(mfrc522.uid.sak);

  Serial.println(mfrc522.PICC_GetTypeName(piccType));
  //end::rdid-card-details[]

  //tag::check-compatibility[]
  if (    piccType != MFRC522::PICC_TYPE_MIFARE_MINI
          &&  piccType != MFRC522::PICC_TYPE_MIFARE_1K
          &&  piccType != MFRC522::PICC_TYPE_MIFARE_4K) {
    Serial.print("\nThis program only works with MIFARE Classic cards.");
    exitMessage();
    return;
  }
  //tag::check-compatibility[]

  Serial.setTimeout(20000L); // wait until 20 seconds for input from serial

  inputBlock("firstname", blockcontent);
 
  if (writeBlock(FIRSTNAME_BLOCKNUMBER , blockcontent) == 1) {
    displayBlock(FIRSTNAME_BLOCKNUMBER, readbackblock);

    memset(blockcontent, 0, sizeof(blockcontent)); //Clear
    memset(readbackblock, 0, sizeof(readbackblock)); //Clear

    inputBlock("lastname", blockcontent);

    if (writeBlock(LASTNAME_BLOCKNUMBER , blockcontent) == 1) {
      displayBlock(LASTNAME_BLOCKNUMBER, readbackblock);

      memset(blockcontent, 0, sizeof(blockcontent)); //Clear
      memset(readbackblock, 0, sizeof(readbackblock)); //Clear

      inputBlock("code", blockcontent);
         
      if (writeBlock(CODE_BLOCKNUMBER , blockcontent) == 1) {
        displayBlock(CODE_BLOCKNUMBER, readbackblock);
      }
    }
  }

  mfrc522.PICC_HaltA();      // Halt PICC
  mfrc522.PCD_StopCrypto1(); // Stop encryption on PCD

  exitMessage();
}
