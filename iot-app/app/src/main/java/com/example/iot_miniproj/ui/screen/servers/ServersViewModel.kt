package com.example.iot_miniproj.ui.screen.servers

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.iot_miniproj.message.MessageManager
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.net.InetAddress

class ServersViewModel(private val application: Application) : ViewModel() {
    private val servers = mutableStateListOf<ServerState>()

    init {
        loadFile()
    }

    fun addServer(serverParams: ServerParams) {
        val addr = InetAddress.getByName(serverParams.address)
        val messageManager = MessageManager(addr, serverParams.port)
        val state = ServerState(addr, serverParams.port, messageManager) {removeServer(it)}
        servers.add(state)
        saveFile()
    }

    fun removeServer(s: ServerState) {
        servers.remove(s)
        saveFile()
    }

    private fun loadFile() {
        val file = File(application.filesDir, "servers.json")
        if (file.exists()) {
            val content = file.readText()
            if (content.isNotEmpty()) {
                val serversParams = Json.decodeFromString<List<ServerParams>>(content)
                for (s in serversParams) {
                    addServer(s)
                }
            }
        }
    }

    private fun saveFile() {
        val file = File(application.filesDir, "servers.json")
        if (!file.exists()) {
            file.createNewFile()
        }
        val content = Json.encodeToString(servers.map { ServerParams(it.getAdresse(), it.getIntPort()) })
        file.writeText(content)
    }

    fun getServers(): List<ServerState> {
        return servers
    }

    fun onPause() {
        for (s in servers) {
            s.pause()
        }
    }

    fun onResume() {
        for (s in servers) {
            s.resume()
        }
    }

    override fun onCleared() {
        super.onCleared()
        for (s in servers) {
            s.stop()
        }
    }
}