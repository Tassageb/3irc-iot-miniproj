package com.example.iot_miniproj

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.iot_miniproj.message.data.SensorType
import com.example.iot_miniproj.ui.composable.reorderablelist.ReorderableLazyColumn
import com.example.iot_miniproj.ui.theme.IotminiprojTheme

class UpdateDisplayModeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val initMode = intent.getStringExtra("current_mode")
            ?: throw IllegalArgumentException("too few arguments")

        val items = initMode.map { c -> SensorType.valueOf(c.toString()) }
            .distinct()
            .toCollection(ArrayList())
        for (e in SensorType.entries) {
            if (!items.contains(e)) {
                items.add(e)
            }
        }

        enableEdgeToEdge()
        setContent {
            UpdateDisplayModeScreen(items) {
                val resultIntent = Intent().apply {
                    Log.d("RESULT", items.map { s -> s.name }.reduce({ s1, s2 -> s1 + s2 }))
                    putExtra(
                        "target_mode",
                        items.map { s -> s.name }.reduce({ s1, s2 -> s1 + s2 })
                    )
                    putExtra("room_id", intent.getStringExtra("room_id"))
                    putExtra(
                        "server_params",
                        intent.getStringExtra("server_params")
                    )
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }
}

@Composable
fun UpdateDisplayModeScreen(items: ArrayList<SensorType>, submit: () -> Unit) {
    IotminiprojTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ReorderableLazyColumn(
                    Modifier
                        .padding(innerPadding)
                        .weight(1f),
                    items,
                    {}) { modifier, sensorType ->
                    Card(modifier = modifier) {
                        Text(
                            sensorType.label,
                            modifier = modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        )
                    }
                }
                Button(
                    modifier = Modifier.padding(16.dp),
                    onClick = submit) {
                    Text("Valider")
                }
            }

        }
    }
}
