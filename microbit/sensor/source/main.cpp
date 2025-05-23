#include "MicroBit.h"
#include "tsl256x.h"
#include "bme280.h"
#include "ssd1306.h"

struct SensorPacket {
    int receiver;
    char* message;
};

MicroBit uBit;
MicroBitI2C i2c(I2C_SDA0,I2C_SCL0);
MicroBitPin P0(MICROBIT_ID_IO_P0, MICROBIT_PIN_P0, PIN_CAPABILITY_DIGITAL_OUT);
ssd1306 screen(&uBit, &i2c, &P0);

tsl256x tsl(&uBit,&i2c);
bme280 bme(&uBit,&i2c);

char sensorMode[6] = "TLH";

const int CIPHER_KEY = 23;

int mod(int num, int mod) {
    int nummod = num % mod;
    if (nummod < 0) {
        return nummod + mod;
    }
    return nummod;
}

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
    int sn = mod(static_cast<int>(microbit_serial_number()), 1000);
    ManagedString s = uBit.radio.datagram.recv();
    
    if (s.length() == 0) {
        return;
    }

    if (deserialize_sensor_packet(&p, s) == -1) {
        return;
    }

    if (p.receiver != sn) {
        return;
    }
    
    strncpy(sensorMode, p.message, 6);
}

int get_sensor_value(char sensor) {
    if (sensor == 'T') {
        int32_t temp;
        bme.sensor_read(NULL, &temp, NULL);
        return static_cast<int>(bme.compensate_temperature(temp));
    }
    if (sensor == 'L') {
        uint32_t lux;
        tsl.sensor_read(NULL, NULL, &lux);
        return static_cast<int>(lux);
    }
    if (sensor == 'H') {
        uint16_t hum;
        bme.sensor_read(NULL, NULL, &hum);
        return static_cast<int>(bme.compensate_humidity(hum));
    }
    if (sensor == 'P') {
        uint32_t pr;
        bme.sensor_read(&pr, NULL, NULL);
        return static_cast<int>(bme.compensate_pressure(pr));
    }
    if (sensor == 'I') {
        uint16_t ir;
        tsl.sensor_read(NULL, &ir, NULL);
        return static_cast<int>(ir);
    }
    return -1;
}

void display_sensor_value(char sensor, int index, int value) {
    char line[17];

    switch (sensor) {
        case 'T':
            snprintf(line, 16, "Temp: %d.%dC", value / 100, value % 100);
            break;
        case 'L':
            snprintf(line, 16, "Lum:  %dlux", value);
            break;
        case 'H':
            snprintf(line, 16, "Hum:  %d.%d%%", value / 100, value % 100);
            break;
        case 'P':
            snprintf(line, 16, "Pres: %d.%dmB", value / 100, value % 100);
            break;
        case 'I':
            snprintf(line, 16, "IR:   %dlux", value);
            break;
        default:
            snprintf(line, 16, "N/A:  N/A");
            break;
    }

    for (int i = strlen(line); i < 17; i++) {
        line[i] = ' ';
    }
    line[17] = '\0';
    screen.display_line(index + 1, 0, line);
}

int main() {
    uBit.init();
    int sn = mod(static_cast<int>(microbit_serial_number()), 1000);

    uBit.messageBus.listen(MICROBIT_ID_RADIO, MICROBIT_RADIO_EVT_DATAGRAM, on_data);
    uBit.radio.enable();
    uBit.radio.setGroup(35);

    while(1) {
        char message[64] = "";
        int sensorIndex = 0;
        char sensorValues[58] = "";
        char currentValue[8] = "";
        strcat(message, sensorMode);

        ManagedString line = ManagedString("Mode: ") + ManagedString(sensorMode);
        screen.display_line(0, 0, line.toCharArray());
        while(sensorMode[sensorIndex] != '\0') {
            if (sensorIndex != 0) {
                strcat(sensorValues, ",");
            }
            int value = get_sensor_value(sensorMode[sensorIndex]);
            display_sensor_value(sensorMode[sensorIndex], sensorIndex, value);
            sprintf(currentValue, "%d", value);
            strcat(sensorValues, currentValue);
            sensorIndex++;
        }
        screen.update_screen();

        sprintf(message, "%s:%s", sensorMode, sensorValues);

        SensorPacket p = { sn, message };
        send_message(p);

        uBit.sleep(500);
    }
}