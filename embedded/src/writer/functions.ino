
int writeBlock(int blockNumber, byte arrayAddress[])
{
  //tag::prepare-block[]
  // this makes sure that we only write into data blocks. Every 4th block is a trailer block for the access/security info.
  int largestModulo4Number = blockNumber / 4 * 4;
  int trailerBlock = largestModulo4Number + 3; //determine trailer block for the sector
  Serial.print("\n");
  if (blockNumber > 2 && (blockNumber + 1) % 4 == 0) {
    Serial.print(blockNumber);  //block number is a trailer block (modulo 4); quit and send error code 2
    Serial.print(" is a trailer block.");
    return 2;
  }
  //end::prepare-block[]

  Serial.print(blockNumber);
  Serial.print(" is a data block. ");

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

  if (status != MFRC522::STATUS_OK) {
    Serial.print("PCD_Authenticate() failed: ");
    Serial.print(mfrc522.GetStatusCodeName(status));
    Serial.print("\n");
    return 3;//return "3" as error message
  }
  // it appears the authentication needs to be made before every block 
  // read/write within a specific sector.
  // If a different sector is being authenticated access to the previous one is lost.
  //end::authentication-block[]

  //tag::writing-block[]

  // valueBlockA is the block number, MIFARE_Write(block number (0-15), 
  // byte array containing 16 values, number of bytes in block (=16))
  status = mfrc522.MIFARE_Write(blockNumber, arrayAddress, 16);
  //status = mfrc522.MIFARE_Write(9, value1Block, 16);
  if (status != MFRC522::STATUS_OK) {
    Serial.print("MIFARE_Write() failed: ");
    Serial.print(mfrc522.GetStatusCodeName(status));
    Serial.print("\n");
    return 4;//return "4" as error message
  }
  //end::writing-block[]

  Serial.println("Block was written");

  return 1;
}

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
  if (status != MFRC522::STATUS_OK) {
    Serial.print("PCD_Authenticate() failed (read): ");
    Serial.print(mfrc522.GetStatusCodeName(status));
    Serial.print("\n");
    return 3;//return "3" as error message
  }
  //it appears the authentication needs to be made before every block read/write within a specific sector.
  //If a different sector is being authenticated access to the previous one is lost.
  //end::authentication-block[]

  //tag::reading-block[]
  // we need to define a variable with the read buffer size, since the MIFARE_Read
  // method below needs a pointer to the variable that contains the size...
  byte buffersize = 18;
  // &buffersize is a pointer to the buffersize variable; MIFARE_Read requires a pointer instead of just a number
  status = mfrc522.MIFARE_Read(blockNumber, arrayAddress, &buffersize);
  if (status != MFRC522::STATUS_OK) {
    Serial.print("MIFARE_read() failed: ");
    Serial.print(mfrc522.GetStatusCodeName(status));
    Serial.print("\n");
    return 4;//return "4" as error message
  }

  Serial.println("Block was read");
  return 1;
}

void inputBlock(String label, byte *blockcontent) {
  Serial.print("\nType " + label + ", ending with #");
  byte lenght = Serial.readBytesUntil('#', (char *)blockcontent, 18); // read input from serial

  for (byte i = lenght; i < 18; i++)
    blockcontent[i] = '_'; // pad with spaces
}

void displayBlock(int blocknumber, byte *readbackblock) {
  readBlock(blocknumber, readbackblock);//read the block back
  Serial.print("Read block: ");

  for (int j = 0 ; j < 16 ; j++) //print the block contents
  {
    Serial.write (readbackblock[j]);//Serial.write() transmits the ASCII numbers as human readable characters to serial monitor
  }

  Serial.println("");
}

void exitMessage() {
  Serial.print("\n**** Take away the RFID-card ****");
  delay(5000); //Some delay to be sure that the current RFID-card was move away
}

void dumpBytetoArray(byte *buffer, byte bufferSize) {
  //Helper routine to dump a byte array as hex values to Serial.
  for (byte i = 0; i < bufferSize; i++) {
    Serial.print(buffer[i] < 0x10 ? " 0" : " ");
    Serial.print(buffer[i], HEX);
  }
}
