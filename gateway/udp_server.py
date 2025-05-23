import socketserver
import threading
import json
import time
from uart_connection import send_UART_message

HOST           = "0.0.0.0"
UDP_PORT       = 10000

AVAILABLE_COMMANDS = ["UPDATE_DISPLAY"]

server = None
callback = None
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
            if obj["type"] in AVAILABLE_COMMANDS: # Send message through UART
                callback(obj["type"], obj["data"])
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

def start_udp_server(cb) :
    global callback, server
    callback = cb
    server = ThreadedUDPServer((HOST, UDP_PORT), ThreadedUDPRequestHandler)
    server_thread = threading.Thread(target=server.serve_forever)
    server_thread.daemon = True

    server_thread.start()
    print("Server started at {} port {}".format(HOST, UDP_PORT))

    return server