package com.example.iot_miniproj.message.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Values(
    @SerialName("room_id") val roomId: String,
    val mode: String,
    val values: Map<SensorType, Int>
)
