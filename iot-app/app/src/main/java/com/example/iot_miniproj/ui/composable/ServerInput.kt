package com.example.iot_miniproj.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import java.net.InetAddress


@Composable
fun ServerInput(
    onSubmit: (addr: String, port: Int) -> Unit
) {
    var addr by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("") }

    fun isValid(): Boolean {
        var parsedPort = port.toIntOrNull()
        if (parsedPort == null || parsedPort < 0 || parsedPort > 65536) {
            return false
        }

        if (addr.isBlank()) {
            return false
        }
        try {
            InetAddress.getByName(addr)
        } catch (e: Exception) {
            return false
        }
        return true
    }

    Column() {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            TextField(
                value = addr,
                onValueChange = { addr = it },
                label = { Text("Adresse") },
                modifier = Modifier.padding(
                    Dp(4F)
                )
            )
            TextField(
                value = port,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                onValueChange = { port = it },
                label = { Text("Port") },
                modifier = Modifier.padding(
                    Dp(4F)
                )
            )
        }
        Button({
            if (isValid()) {
                onSubmit(addr, port.toInt())
                addr = ""
                port = ""
            }
        }) { Text("Connexion") }
    }
}