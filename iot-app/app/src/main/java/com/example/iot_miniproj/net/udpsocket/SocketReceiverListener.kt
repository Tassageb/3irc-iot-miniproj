package com.example.iot_miniproj.net.udpsocket

interface SocketReceiverListener {
    fun onReceive(message: String)
}