package com.example.iot_miniproj

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.iot_miniproj.ui.composition.LocalActivityResultLauncher
import com.example.iot_miniproj.ui.screen.servers.ServerParams
import com.example.iot_miniproj.ui.screen.servers.ServersScreen
import com.example.iot_miniproj.ui.screen.servers.ServersViewModel
import com.example.iot_miniproj.ui.theme.IotminiprojTheme
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: ServersViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ServersViewModel(application)

        val launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result -> //TODO DEPLACE AILLEURS + REFACTOR
            if (result.resultCode == RESULT_OK && result.data != null) {
                val roomId = result.data!!.getStringExtra("room_id")
                val serverParams = result.data!!.getStringExtra("server_params")
                    ?.let { Json.decodeFromString<ServerParams>(it) }
                val targetMode = result.data!!.getStringExtra("target_mode")

                if (serverParams != null && roomId != null && targetMode != null) {
                    val serverState = viewModel.getServers().find {
                        it.getAdresse().equals(serverParams.address) && it.getIntPort()
                            .equals(serverParams.port)
                    }
                    if (serverState != null) {
                        serverState.rooms[roomId]?.updateDisplay(targetMode)
                    }
                }
            }
            Log.d("LAUNCHER RESULT", "result")
        }

        enableEdgeToEdge()
        setContent {
            MainScreen(viewModel, launcher)
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.onPause()
    }

    override fun onRestart() {
        super.onRestart()
        viewModel.onResume()
    }
}

@Composable
fun MainScreen(viewModel: ServersViewModel, launcher: ActivityResultLauncher<Intent>) {
    CompositionLocalProvider(
        LocalActivityResultLauncher provides launcher
    ) {
        IotminiprojTheme {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Column(modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxHeight(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    content = {
                        ServersScreen(viewModel)
                    })

            }
        }
    }
}