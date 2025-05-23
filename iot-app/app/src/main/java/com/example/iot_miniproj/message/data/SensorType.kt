package com.example.iot_miniproj.message.data

enum class SensorType(val label: String) {
    T("Température"),
    H("Humidité"),
    L("Luminosité"),
    I("Infrarouge"),
    P("Pression")
}