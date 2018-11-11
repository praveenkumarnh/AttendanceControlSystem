
byte readBlock(int blockNumber, byte arrayAddress[])
{
  unsigned short int largestModulo4Number = blockNumber / 4 * 4;
  unsigned short int trailerBlock = largestModulo4Number + 3; //determine trailer block for the sector

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
    // Dont print, it need resources
    return 3; //return "3" as error message
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
    // Dont print, it need resources
    return 4; //return "4" as error message
  }

  return 1;
}

String readBlockContent(int blocknumber, byte *readblock)
{
  byte status = readBlock(blocknumber, readblock); //read the block back
  String readstring = "";

  if (status == 1)
  {
    for (unsigned short int j = 0; j < 16; j++) //print the block contents
    {
      readstring += (char)readblock[j];
      //Serial.write(readblock[j]);
      readblock[j] = 0xFF; // Clean again
    }

    readstring.replace("_", ""); //Remove all "utils" chars
    readstring.trim();
  }

  return readstring;
}
