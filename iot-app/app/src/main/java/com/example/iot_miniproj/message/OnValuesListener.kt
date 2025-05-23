package com.example.iot_miniproj.message

import com.example.iot_miniproj.message.data.Values

interface OnValuesListener {
    fun onValue(message: Values)
}