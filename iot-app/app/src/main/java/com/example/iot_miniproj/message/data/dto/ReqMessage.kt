package com.example.iot_miniproj.message.data.dto

import com.example.iot_miniproj.message.data.ReqMessageType
import kotlinx.serialization.Serializable

@Serializable
data class ReqMessage(val type: ReqMessageType, val data: String? = null)
