package com.example.iot_miniproj.message

import com.example.iot_miniproj.message.data.dto.ReqMessage
import com.example.iot_miniproj.message.data.ReqMessageType
import com.example.iot_miniproj.message.data.dto.ResMessage
import com.example.iot_miniproj.message.data.ResMessageType
import com.example.iot_miniproj.message.data.Values
import com.example.iot_miniproj.net.MessagePoller
import com.example.iot_miniproj.net.OnMessagePollerStateChangeListener
import com.example.iot_miniproj.net.udpsocket.UDPMessageSocket
import com.example.iot_miniproj.net.udpsocket.UDPMessageSocketListener
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import java.net.InetAddress

class MessageManager(address: InetAddress, port: Int) {

    private val udpMessageSocket = UDPMessageSocket(address, port);
    private var valuePoller: MessagePoller? = null;

    private var pollingValues = false
    private val valuesListeners: ArrayList<OnValuesListener> = ArrayList();
    private val onPollingValueStateChangeListeners: ArrayList<OnPollingValueStateChangeListener> = ArrayList()

    init {
        udpMessageSocket.addListener(object : UDPMessageSocketListener {
            override fun onMessage(message: String) {
                val decodedMessage = Json.decodeFromString<ResMessage>(message)
                if (ResMessageType.VALUES == decodedMessage.type) {
                    val decodedValues = Json.decodeFromJsonElement<Values>(decodedMessage.data!!)
                    valuesListeners.forEach { it.onValue(decodedValues) }
                }
            }
        })
    }

    fun addValuesListener(listener: OnValuesListener) {
        valuesListeners.add(listener)
    }

    fun addGetValuesStoppedListener(listener: OnPollingValueStateChangeListener) {
        onPollingValueStateChangeListeners.add(listener)
    }

    fun sendMessage(type: ReqMessageType, data: String? = null) {
        udpMessageSocket.sendMessage(Json.encodeToString(ReqMessage(type, data)))
    }

    fun startRetrieveValues() {
        if (valuePoller != null && !valuePoller!!.isClosed()) return;
        valuePoller = MessagePoller(udpMessageSocket, Json.encodeToString(ReqMessage(ReqMessageType.GET_VALUES)))
        valuePoller!!.addStateChangeListener(object : OnMessagePollerStateChangeListener {
            override fun onPollerActive() {
                pollingValues = true
                onPollingValueStateChangeListeners.forEach { it.onPollingValueStarted() }
            }

            override fun onPollerInactive() {
                pollingValues = false
                onPollingValueStateChangeListeners.forEach { it.onPollingValueStopped() }
            }

            override fun onPollerClosed() {
                pollingValues = false
                onPollingValueStateChangeListeners.forEach { it.onPollingValueStopped() }
            }
        })
        valuePoller!!.start()
    }

    fun stopRetrieveValues() {
        valuePoller?.close()
    }

    fun stop() {
        stopRetrieveValues()
        udpMessageSocket.close()
    }

}