package com.example.iot_miniproj.ui.screen.servers

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import com.example.iot_miniproj.message.MessageManager
import com.example.iot_miniproj.message.OnPollingValueStateChangeListener
import com.example.iot_miniproj.message.OnValuesListener
import com.example.iot_miniproj.message.data.Values
import java.net.InetAddress

class ServerState(
    private val address: InetAddress,
    private val port: Int,
    private val messageManager: MessageManager?,
    private val onDelete: (s: ServerState) -> Unit
) {
    val connected = mutableStateOf(false)
    val rooms = mutableStateMapOf<String, RoomState>()


    init {
        if (messageManager != null) {
            messageManager.addGetValuesStoppedListener(object : OnPollingValueStateChangeListener {
                override fun onPollingValueStopped() {
                    connected.value = false
                }

                override fun onPollingValueStarted() {
                    connected.value = true
                }
            })

            messageManager.addValuesListener(object : OnValuesListener {
                override fun onValue(message: Values) {
                    val roomState =
                        rooms.getOrPut(message.roomId) { RoomState(message.roomId, messageManager) }
                    roomState.updateValues(message)
                }
            })

            resume()
        }
    }

    fun getAdresse(): String {
        return if (address.hostAddress != null) address.hostAddress!! else ""
    }

    fun getPort(): String {
        return port.toString()
    }

    fun getIntPort(): Int {
        return port
    }

    fun resume() {
        messageManager?.startRetrieveValues()
    }

    fun pause() {
        messageManager?.stopRetrieveValues()
    }

    fun delete() {
        stop()
        onDelete(this)
    }

    fun stop() {
        messageManager?.stop()
    }
}