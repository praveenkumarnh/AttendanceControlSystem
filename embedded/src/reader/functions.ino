
int readBlock(int blockNumber, byte arrayAddress[])
{
  int largestModulo4Number = blockNumber / 4 * 4;
  int trailerBlock = largestModulo4Number + 3; //determine trailer block for the sector

  //tag::authentication-block[]
  byte status = mfrc522.PCD_Authenticate(MFRC522::PICC_CMD_MF_AUTH_KEY_A, trailerBlock, &key, &(mfrc522.uid));
  // byte PCD_Authenticate(byte command, byte blockAddr, MIFARE_Key *key, Uid *uid);
  // this method is used to authenticate a certain block for writing or reading
  // command: See enumerations above -> PICC_CMD_MF_AUTH_KEY_A	= 0x60 (=1100000),
  // this command performs authentication with Key A
  // blockAddr is the number of the block from 0 to 15.
  // MIFARE_Key *key is a pointer to the MIFARE_Key struct defined above,
  // this struct needs to be defined for each block.
  // New cards have all A/B= FF FF FF FF FF FF
  // Uid *uid is a pointer to the UID struct that contains the user ID of the card.
  if (status != MFRC522::STATUS_OK)
  {
    Serial.print("PCD_Authenticate(read) [FAILED]");
    Serial.print(mfrc522.GetStatusCodeName(status));
    Serial.print("\n");
    return 3; //return "3" as error message
  }
  else
  {
    Serial.print("PCD_Authenticate(read) [0K]\n");
  }
  //it appears the authentication needs to be made before every block read/write within a specific sector.
  //If a different sector is being authenticated access to the previous one is lost.
  //end::authentication-block[]

  //tag::reading-block[]
  // we need to define a variable with the read buffer size, since the MIFARE_Read
  // method below needs a pointer to the variable that contains the size...
  byte buffersize = 18;
  // &buffersize is a pointer to the buffersize variable;
  // MIFARE_Read requires a pointer instead of just a number
  status = mfrc522.MIFARE_Read(blockNumber, arrayAddress, &buffersize);
  if (status != MFRC522::STATUS_OK)
  {
    Serial.print("MIFARE_read() [FAILED] ");
    Serial.print(mfrc522.GetStatusCodeName(status));
    Serial.print("\n");
    return 4; //return "4" as error message
  }

  Serial.println("MIFARE_read() [0K]");
  return 1;
}

void inputBlock(String label, byte *blockcontent)
{
  Serial.print("\nType " + label + ", ending with #");
  byte lenght = Serial.readBytesUntil('#', (char *)blockcontent, 18); // read input from serial

  for (byte i = lenght; i < 18; i++)
    blockcontent[i] = '_'; // pad with spaces
}

int readBlockContent(int blocknumber, byte *readblock, String &readstring)
{
  int status = readBlock(blocknumber, readblock); //read the block back

  if (status == 1)
  {
    for (int j = 0; j < 16; j++) //print the block contents
    {
      //Serial.write() transmits the ASCII numbers as human readable characters to serial monitor
      readstring += (char)readblock[j];
      Serial.write(readblock[j]);
    }

    readstring.replace("_", ""); //Remove all "utils" chars
    readstring.trim();

    Serial.print("\n");
  }

  return status;
}

void dumpBytetoArray(byte *buffer, byte bufferSize)
{
  //Helper routine to dump a byte array as hex values to Serial.
  for (byte i = 0; i < bufferSize; i++)
  {
    Serial.print(buffer[i] < 0x10 ? " 0" : " ");
    Serial.print(buffer[i], HEX);
  }
}

void userSignal()
{

  int signals[] = {
    262, 196, 196, 220, 196, 0, 247, 262
  };

  int durations[] = {
    4, 8, 8, 4, 4, 8, 4, 4
  };

  for (int i = 0; i < 8; i++)
  {

    int duration = 1500 / durations[i];
    tone(LED_PIN, signals[i], duration);

    int pause = duration * 1.40;
    
    delay(pause);

    noTone(LED_PIN);
  }
}
