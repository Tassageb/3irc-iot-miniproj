package com.example.iot_miniproj.net.udpsocket

interface UDPMessageSocketListener {
    fun onMessage(message: String)
}