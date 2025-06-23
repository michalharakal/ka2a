package sk.ai.net.solutions.ka2a.samples.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import sk.ai.net.solutions.ka2a.samples.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    MaterialTheme {
        val viewModel = remember { AppViewModel() }
        val state by viewModel.state.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("A2A Client Sample") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { paddingValues ->
            Surface(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    ConnectionPanel(
                        serverUrl = state.serverUrl,
                        isConnected = state.isConnected,
                        isConnecting = state.isConnecting,
                        onServerUrlChanged = viewModel::updateServerUrl,
                        onConnectClicked = viewModel::connect,
                        onDisconnectClicked = viewModel::disconnect
                    )

                    ActionPanel(
                        isConnected = state.isConnected,
                        onPingClicked = viewModel::ping,
                        onDiscoverClicked = viewModel::discover,
                        onTranslateClicked = { text, language ->
                            viewModel.translate(text, language)
                        }
                    )

                    LogPanel(logs = state.logs)

                    state.error?.let { error ->
                        ErrorMessage(error = error)
                    }
                }
            }
        }
    }
}
