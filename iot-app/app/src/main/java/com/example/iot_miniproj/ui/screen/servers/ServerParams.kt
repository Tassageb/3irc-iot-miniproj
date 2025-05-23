package com.example.iot_miniproj.ui.screen.servers

import kotlinx.serialization.Serializable

@Serializable
data class ServerParams(
    val address: String,
    val port: Int,
)
