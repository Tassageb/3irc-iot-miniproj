package com.example.iot_miniproj.ui.screen.servers

import androidx.compose.runtime.mutableStateOf
import com.example.iot_miniproj.message.MessageManager
import com.example.iot_miniproj.message.data.ReqMessageType
import com.example.iot_miniproj.message.data.SensorType
import com.example.iot_miniproj.message.data.Values
import java.util.Optional

class RoomState(val id: String, private val messageManager: MessageManager) {
    val luminosite = mutableStateOf<Int?>(null);
    val humidite = mutableStateOf<Float?>(null);
    val temperature = mutableStateOf<Float?>(null);
    val infrarouge = mutableStateOf<Int?>(null);
    val pression = mutableStateOf<Float?>(null);
    val mode = mutableStateOf("");

    fun updateValues(values: Values) {
        mode.value = values.mode
        luminosite.value = Optional.ofNullable(values.values[SensorType.L]).orElse(null)
        humidite.value =
            Optional.ofNullable(values.values[SensorType.H]).map { it / 100F }.orElse(null)
        temperature.value =
            Optional.ofNullable(values.values[SensorType.T]).map { it / 100F }.orElse(null)
        infrarouge.value = Optional.ofNullable(values.values[SensorType.I]).orElse(null)
        pression.value =
            Optional.ofNullable(values.values[SensorType.P]).map { it / 100F }.orElse(null)
    }

    fun updateDisplay(mode: String) {
        messageManager.sendMessage(ReqMessageType.UPDATE_DISPLAY, this.id + ":" + mode)
    }

}