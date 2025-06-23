package sk.ai.net.solutions.ka2a.samples.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import sk.ai.net.solutions.ka2a.client.A2AClient
import sk.ai.net.solutions.ka2a.client.DefaultA2AClient
import sk.ai.net.solutions.ka2a.client.model.A2AResponse
import sk.ai.net.solutions.ka2a.client.model.A2AStatusCode
import sk.ai.net.solutions.ka2a.client.model.Destination
import sk.ai.net.solutions.ka2a.client.model.Payload
import sk.ai.net.solutions.ka2a.samples.model.AppState
import sk.ai.net.solutions.ka2a.samples.model.LogEntry
import sk.ai.net.solutions.ka2a.samples.model.LogType

class AppViewModel {
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default)
    private var client: A2AClient? = null

    fun updateServerUrl(url: String) {
        _state.update { it.copy(serverUrl = url) }
    }

    fun connect() {
        val serverUrl = state.value.serverUrl
        if (serverUrl.isBlank()) {
            addLog(LogType.ERROR, "Server URL cannot be empty")
            return
        }

        _state.update { it.copy(isConnecting = true, error = null) }
        addLog(LogType.INFO, "Checking server availability at $serverUrl...")

        client = DefaultA2AClient(
            appId = "ka2a-sample-app",
            appType = "web-client",
            //appUrl = "localhost",
        )

        scope.launch {
            // First check if the server is available
            client?.checkServerAvailability(serverUrl)?.collect { isAvailable ->
                if (!isAvailable) {
                    _state.update { it.copy(isConnected = false, isConnecting = false, error = "Server not available") }
                    addLog(LogType.ERROR, "Server not available at $serverUrl. Make sure the server is running and the URL is correct.")
                    return@collect
                }

                addLog(LogType.INFO, "Server is available. Connecting...")

                val destination = Destination(
                    id = "a2a-server",
                    type = "server",
                    url = serverUrl
                )

                try {
                    client?.ping(destination)?.collect { response ->
                        handleResponse(response)
                        if (response.status.code == A2AStatusCode.SUCCESS.code) {
                            _state.update { it.copy(isConnected = true, isConnecting = false) }
                            addLog(LogType.INFO, "Connected to server")
                        } else {
                            _state.update { it.copy(isConnected = false, isConnecting = false, error = response.status.message) }
                            addLog(LogType.ERROR, "Failed to connect: ${response.status.message}")
                        }
                    }
                } catch (e: Exception) {
                    _state.update { it.copy(isConnected = false, isConnecting = false, error = e.message) }
                    addLog(LogType.ERROR, "Connection error: ${e.message}")
                }
            }
        }
    }

    fun disconnect() {
        client = null
        _state.update { it.copy(isConnected = false) }
        addLog(LogType.INFO, "Disconnected from server")
    }

    fun ping() {
        executeAction("Ping") { client, destination ->
            client.ping(destination)
        }
    }

    fun discover() {
        executeAction("Discover") { client, destination ->
            client.discover(destination)
        }
    }

    fun translate(text: String, targetLanguage: String) {
        val payload = Payload(
            content = mapOf(
                "text" to text,
                "targetLanguage" to targetLanguage
            )
        )

        executeAction("Translate") { client, destination ->
            client.customAction(destination, "translate", payload)
        }
    }

    private fun executeAction(
        actionName: String,
        action: (A2AClient, Destination) -> kotlinx.coroutines.flow.Flow<A2AResponse>
    ) {
        val currentState = state.value
        if (!currentState.isConnected) {
            addLog(LogType.ERROR, "Not connected to server")
            return
        }

        val currentClient = client ?: run {
            addLog(LogType.ERROR, "Client not initialized")
            return
        }

        val destination = Destination(
            id = "a2a-server",
            type = "server",
            url = currentState.serverUrl
        )

        addLog(LogType.REQUEST, "Sending $actionName request...")

        scope.launch {
            try {
                action(currentClient, destination).collect { response ->
                    handleResponse(response)
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
                addLog(LogType.ERROR, "$actionName error: ${e.message}")
            }
        }
    }

    private fun handleResponse(response: A2AResponse) {
        _state.update { it.copy(lastResponse = response) }

        val logType = if (response.status.code == A2AStatusCode.SUCCESS.code) {
            LogType.RESPONSE
        } else {
            LogType.ERROR
        }

        val message = "Response: ${response.status.code} ${response.status.message ?: ""}"
        addLog(logType, message)

        if (response.payload != null) {
            addLog(logType, "Payload: ${response.payload}")
        }
    }

    private fun addLog(type: LogType, message: String) {
        val logEntry = LogEntry(
            type = type,
            message = message
        )

        _state.update { currentState ->
            val newLogs = currentState.logs.toMutableList().apply {
                add(0, logEntry) // Add to the beginning for newest first
                if (size > 100) {
                    removeAt(size - 1) // Remove oldest if more than 100
                }
            }
            currentState.copy(logs = newLogs)
        }
    }
}
