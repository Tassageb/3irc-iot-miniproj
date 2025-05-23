# Program to control passerelle between Android application
# and micro-controller through USB tty
# import time
# import argparse
# import signal
# import sys
# import socket
import socketserver
from time import sleep
import serial
import threading
import json
import time

HOST           = "0.0.0.0"
UDP_PORT       = 10000
MICRO_COMMANDS = ["UPDATE_DISPLAY"]
FILENAME        = "values.txt"

active_listeners = {}
listeners_lock = threading.Lock()
def register_listener(client_address):
    expire_time = time.time() + 5  # valable 5 secondes
    with listeners_lock:
        active_listeners[client_address] = expire_time

def get_valid_listeners():
    now = time.time()
    with listeners_lock:
        return {k: v for k, v in active_listeners.items() if v > now}
    
def send_to_listeners(msg):
    payload = json.dumps(msg).encode("utf-8")
    listeners = get_valid_listeners()
    current_thread = threading.current_thread()
    for addr in listeners:
        print("{}: sending to: {}, wrote: {}".format(current_thread.name, addr, payload))
        server.socket.sendto(payload, addr)


class ThreadedUDPRequestHandler(socketserver.BaseRequestHandler):
    def handle(self):
        data = self.request[0].strip().decode("utf-8")
        socket = self.request[1]
        current_thread = threading.current_thread()
        print("{}: client: {}, wrote: {}".format(current_thread.name, self.client_address, data))
        if data != "":
            obj = json.loads(data)
            if obj["type"] in MICRO_COMMANDS: # Send message through UART
                sendUARTMessage(obj["data"]+"!")
            elif obj["type"] == "GET_VALUES": # Sent last value received from micro-controller
                msg = {
                    "type": "PING"
                }
                payload = json.dumps(msg).encode("utf-8")
                addr = self.client_address
                print("{}: sending to: {}, wrote: {}".format(current_thread.name, addr, payload))
                socket.sendto(payload, addr)
                register_listener(addr)   
            else:
                print("Unknown message: ",data)

class ThreadedUDPServer(socketserver.ThreadingMixIn, socketserver.UDPServer):
    pass

# send serial message 
SERIALPORT = "/dev/ttyACM0"
#SERIALPORT = "/dev/pts/3"
BAUDRATE = 115200
ser = serial.Serial()

def initUART():        
        # ser = serial.Serial(SERIALPORT, BAUDRATE)
        ser.port=SERIALPORT
        ser.baudrate=BAUDRATE
        ser.bytesize = serial.EIGHTBITS #number of bits per bytes
        ser.parity = serial.PARITY_NONE #set parity check: no parity
        ser.stopbits = serial.STOPBITS_ONE #number of stop bits
        ser.timeout = None          #block read

        # ser.timeout = 0             #non-block read
        # ser.timeout = 2              #timeout block read
        ser.xonxoff = False     #disable software flow control
        ser.rtscts = False     #disable hardware (RTS/CTS) flow control
        ser.dsrdtr = False       #disable hardware (DSR/DTR) flow control
        #ser.writeTimeout = 0     #timeout for write
        print('Starting Up Serial Monitor')
        try:
                ser.open()
        except serial.SerialException:
                print("Serial {} port not available".format(SERIALPORT))
                exit()



def sendUARTMessage(msg):
    ser.write(msg.encode())
    print("Message <" + msg + "> sent to micro-controller." )


# Main program logic follows:
if __name__ == '__main__':
        initUART()
        f= open(FILENAME,"a")
        print ('Press Ctrl-C to quit.')

        server = ThreadedUDPServer((HOST, UDP_PORT), ThreadedUDPRequestHandler)
        server_thread = threading.Thread(target=server.serve_forever)
        server_thread.daemon = True

        try:
            server_thread.start()
            print("Server started at {} port {}".format(HOST, UDP_PORT))
            first = True
            buffer = ""
            while ser.isOpen() : 
                if (ser.inWaiting() > 0): # if incoming bytes are waiting 
                    chunk = ser.read(ser.inWaiting()).decode("utf-8")
                    buffer = buffer + chunk
                    while "\n" in buffer :
                        splitted = buffer.split("\n")
                        data_str = splitted[0]
                        buffer = "\n".join(splitted[1:])
                        data_splitted = data_str.split(":")
                        if data_str == "" or len(data_splitted) < 3 :
                            continue
                        current_thread = threading.current_thread()
                        print("{}: received from uart: {}".format(current_thread.name, data_str))
                        room_id, mode, values_list = data_str.split(":")
                        values_parsed_list = values_list.split(",")
                        values = {k: values_parsed_list[i] for i, k in enumerate(mode) }
                        msg = {
                            "type": "VALUES",
                            "data": {
                                "room_id": room_id,
                                "mode": mode,
                                "values": values
                            }
                        }
                        send_to_listeners(msg)

        except (KeyboardInterrupt, SystemExit):
                server.shutdown()
                server.server_close()
                f.close()
                ser.close()
                exit()