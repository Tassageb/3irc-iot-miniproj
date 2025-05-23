package com.example.iot_miniproj.net

interface OnMessagePollerStateChangeListener {
    fun onPollerActive()
    fun onPollerInactive()
    fun onPollerClosed()
}