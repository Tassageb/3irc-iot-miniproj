package com.example.iot_miniproj.ui.screen.servers

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.iot_miniproj.UpdateDisplayModeActivity
import com.example.iot_miniproj.ui.composable.ServerInput
import com.example.iot_miniproj.ui.composition.LocalActivityResultLauncher
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.InetAddress

@Composable
fun ServersScreen(
    viewModel: ServersViewModel
) {
    val servers = viewModel.getServers()

    ServerInput { addr: String, port: Int -> viewModel.addServer(ServerParams(addr, port)) }
    ServersList(servers)
}


@Composable
fun ServersList(servers: List<ServerState>) {
    LazyColumn {
        items(servers.size) { i ->
            ServerCard(servers[i])
        }
    }
}

@Composable
fun ServerCard(server: ServerState = ServerState(InetAddress.getByName(""), 0, null) {}) {
    val connectedState by server.connected
    val rooms = server.rooms.entries.toList()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.heightIn(max = 450.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Serveur : ${server.getAdresse()}:${server.getPort()}")
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (connectedState) {
                            Icon(Icons.Outlined.Check, contentDescription = null)
                        }
                        ServerMenu(server)
                    }
                }

                LazyColumn {
                    items(rooms.size) { i ->
                        Room(server, rooms[i].value)
                    }
                }
                if (!connectedState) {
                    Text(
                        "Connexion perdu... Tentative de reconnexion",
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 12.sp
                    )
                }
            }
        }

    }
}

@Composable
fun ServerMenu(server: ServerState) {
    var expanded by remember { mutableStateOf(false) }

    Box() {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Supprimer") },
                leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
                onClick = { server.delete() }
            )
        }
    }
}

@Composable
fun Room(server: ServerState, room: RoomState) {
    val tempState by room.temperature
    val humState by room.humidite
    val lumState by room.luminosite
    val presState by room.pression
    val infraState by room.infrarouge
    val modeState by room.mode

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Salle ${room.id}", fontSize = 12.sp)
                RoomMenu(server, room)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(if (tempState != null) "Temp : $tempState °C" else "", fontSize = 10.sp)
                Text(if (humState != null) "Hum : $humState %" else "", fontSize = 10.sp)
                Text(if (lumState != null) "Lum : $lumState lux" else "", fontSize = 10.sp)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(if (presState != null) "Pres : $presState mBar" else "", fontSize = 10.sp)
                Text(if (infraState != null) "Infr : $infraState lux" else "", fontSize = 10.sp)
                Text("Mode : $modeState", fontSize = 10.sp)
            }

        }

    }
}

@Composable
fun RoomMenu(server: ServerState, room: RoomState) {
    val launcher = LocalActivityResultLauncher.current
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Box() {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 200.dp)
        ) {
            DropdownMenuItem(
                text = { Text("Modifier le mode d'affichage") },
                onClick = {
                    val intent = Intent(context, UpdateDisplayModeActivity::class.java)
                    intent.putExtra("room_id", room.id)
                    intent.putExtra("server_params", Json.encodeToString(ServerParams(server.getAdresse(), server.getIntPort()))) //Pas propre mais malgré le @Serializable, ça ne foncttionne pas
                    intent.putExtra("current_mode", room.mode.value)
                    launcher.launch(intent)
                    expanded = false
                }
            )
        }
    }
}