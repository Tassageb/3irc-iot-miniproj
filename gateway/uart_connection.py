from time import sleep
import serial
import threading

SERIALPORT = "/dev/ttyACM0"
BAUDRATE = 115200
ser = serial.Serial()

def init_UART(callback):        
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
    
    thread = threading.Thread(target=UART_worker, args=(ser, callback))
    thread.daemon = True
    thread.start()

    return ser

def send_UART_message(msg):
    ser.write(msg.encode())
    print("Message <" + msg + "> sent to micro-controller." )

def UART_worker(ser, callback):
    buffer = ""
    while ser.isOpen():
        if ser.inWaiting() > 0:
            chunk = ser.read(ser.inWaiting()).decode("utf-8")
            buffer += chunk
            while "\n" in buffer:
                splitted = buffer.split("\n")
                data_str = splitted[0]
                buffer = "\n".join(splitted[1:])
                if data_str == "":
                    continue
                print(f"[UART] Received: {data_str}")
                callback(data_str) 


