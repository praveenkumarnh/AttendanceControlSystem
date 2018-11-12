# AttendanceControlSystem

The original attempt was't going to need an RTC module, since it was thought to synchronize it with the server time (via EthernetClient), 
and then calculate the time of the entries with the milliseconds since the Arduino was turned on, but the limited RAM memory of our 
Arduino UNO did't allow it, the RAM memory became unstable; then in the future we'lll add an RTC.

## Components

- Arduino UNO x 1
- Ethernet Shield x 1
- RFID-RC522 x 1
- Micro SD card x 1
- 33Ω Resistor x 1
- Green led x 1

## Schemas

![schematics](https://raw.githubusercontent.com/rad8329/AttendanceControlSystem/master/embedded/schematics/fritzing_bb.png)


