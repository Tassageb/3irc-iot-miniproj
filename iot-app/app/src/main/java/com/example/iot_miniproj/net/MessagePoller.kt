package com.example.iot_miniproj.net

import com.example.iot_miniproj.net.udpsocket.UDPMessageSocket
import com.example.iot_miniproj.net.udpsocket.UDPMessageSocketListener

class MessagePoller(
    private val udpMessageSocket: UDPMessageSocket,
    private val message: String,
    private val autoClose: Boolean = false
) : Thread() {

    private val TIMEOUT: Long = 5000
    private val RETRY: Int = 0

    private var keep = true;
    private var packetReceived = 0;
    private var fails = 0;
    private var active = false;

    private val stateChangeListener: ArrayList<OnMessagePollerStateChangeListener> = ArrayList()

    fun addStateChangeListener(listener: OnMessagePollerStateChangeListener) {
        stateChangeListener.add(listener)
    }

    fun close() {
        stateChangeListener.forEach { it.onPollerInactive() }
        active = false
        keep = false;
    }

    fun isClosed(): Boolean {
        return !keep;
    }

    override fun run() {
        keep = true;

        udpMessageSocket.addListener(object : UDPMessageSocketListener {
            override fun onMessage(message: String) {
                packetReceived++;
                fails = 0
                if (!active) {
                    active = true
                    stateChangeListener.forEach { it.onPollerActive() }
                }
            }
        })
        while (keep) {
            udpMessageSocket.sendMessage(message);
            sleep(TIMEOUT);
            if (packetReceived == 0) {
                println("poller failed")
                fails++;
                if (fails > RETRY) {
                    if (active) {
                        active = false
                        stateChangeListener.forEach { it.onPollerInactive() }
                    }
                    if (autoClose) {
                        keep = false;
                    }
                }
            }
            packetReceived = 0;
        }
        stateChangeListener.forEach { it.onPollerClosed() }
    }

}