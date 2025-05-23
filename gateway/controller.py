import uart_connection
import udp_server

def onUDPCommand(command, payload):
    #print(command, payload)
    uart_connection.send_UART_message(payload+"!")

def onUartMessage(msg):
    data_splitted = msg.split(":")
    if len(data_splitted) < 3 :
        return
    room_id, mode, values_list = data_splitted
    values_parsed_list = values_list.split(",")
    values = {k: values_parsed_list[i] for i, k in enumerate(mode) }
    payload = {
        "type": "VALUES",
        "data": {
            "room_id": room_id,
            "mode": mode,
            "values": values
        }
    }
    udp_server.send_to_listeners(payload)

if __name__ == "__main__" :
    server = udp_server.start_udp_server(onUDPCommand)
    serial = uart_connection.init_UART(onUartMessage)
    print ('Press Ctrl-C to quit.')

    try :
        while True :
            pass
    except (KeyboardInterrupt, SystemExit):
        server.shutdown()
        server.server_close()
        serial.close()
        exit()
