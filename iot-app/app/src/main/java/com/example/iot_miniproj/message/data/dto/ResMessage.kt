package com.example.iot_miniproj.message.data.dto

import com.example.iot_miniproj.message.data.ResMessageType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ResMessage(val type: ResMessageType, val data: JsonElement? = null)
