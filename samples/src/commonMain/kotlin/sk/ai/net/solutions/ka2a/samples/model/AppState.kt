package sk.ai.net.solutions.ka2a.samples.model

import kotlinx.datetime.Clock
import sk.ai.net.solutions.ka2a.models.A2AResponse

/**
 * Represents the state of the application.
 */
data class AppState(
    val serverUrl: String = "http://localhost:8090",
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val logs: List<LogEntry> = emptyList(),
    val lastResponse: A2AResponse? = null,
    val error: String? = null
)

/**
 * Represents a log entry in the application.
 */
data class LogEntry(
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
    val type: LogType,
    val message: String
)

/**
 * Represents the type of a log entry.
 */
enum class LogType {
    INFO,
    REQUEST,
    RESPONSE,
    ERROR
}
