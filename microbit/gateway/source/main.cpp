#include "MicroBit.h"

MicroBit uBit;
const int CIPHER_KEY = 23;

struct SensorPacket {
    int receiver;
    char* message;
};

// A^B^B=A : Money, Money, Money
void xor_cipher(char* message, int key) {
    for (int i = 0; message[i] != '\0'; ++i) {
        message[i] ^= key;
    }
}

void serialize_sensor_packet(char* buffer, SensorPacket p) {
    snprintf(buffer, 64, "%d:%s", p.receiver, p.message);
    xor_cipher(buffer, CIPHER_KEY);
}

void send_message(SensorPacket p) {
    char buffer[64];
    serialize_sensor_packet(buffer, p);
    uBit.radio.datagram.send((uint8_t*) buffer, strlen(buffer));
}

int deserialize_sensor_packet(SensorPacket *p, ManagedString packet) {
    int sa;
    char buffer[64];
    memcpy(buffer, packet.toCharArray(), packet.length());
    buffer[packet.length()] = '\0';
    xor_cipher(buffer, CIPHER_KEY);
    sa = sscanf(buffer, "%d:%s", &p->receiver, p->message);
    return sa == 2 ? 0 : -1;
}

void on_data(MicroBitEvent) {
    char message[64];
    SensorPacket p = { -1, message };
    ManagedString s = uBit.radio.datagram.recv();
    
    if (s.length() == 0) {
        return;
    }

    if (deserialize_sensor_packet(&p, s) == -1) {
        return;
    }

    char buffer[65];
    snprintf(buffer, 65, "%d:%s\n", p.receiver, p.message);
    uBit.serial.send(buffer);
}

int main() {
    uBit.init();

    uBit.messageBus.listen(MICROBIT_ID_RADIO, MICROBIT_RADIO_EVT_DATAGRAM, on_data);
    uBit.radio.enable();
    uBit.radio.setGroup(35);

    while(1) {
        ManagedString command = uBit.serial.readUntil('!');
        if (command.length() > 0) {
            char buffer[64];
            memcpy(buffer, command.toCharArray(), command.length());
            buffer[command.length()] = '\0';
            xor_cipher(buffer, CIPHER_KEY);
            uBit.radio.datagram.send((uint8_t*) buffer, strlen(buffer));
        }
    }
}