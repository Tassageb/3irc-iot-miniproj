package com.example.iot_miniproj.message

interface OnPollingValueStateChangeListener {
    fun onPollingValueStopped()
    fun onPollingValueStarted()
}