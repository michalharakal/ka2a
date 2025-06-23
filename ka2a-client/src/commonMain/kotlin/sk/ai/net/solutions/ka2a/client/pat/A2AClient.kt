package sk.ai.net.solutions.ka2a.client.pat

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.plugins.sse.SSEConfig
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.utils.io.core.Closeable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import sk.ai.net.solutions.ka2a.client.util.UuidUtil
import sk.ai.net.solutions.ka2a.models.AgentCard
import sk.ai.net.solutions.ka2a.models.GetTaskRequest
import sk.ai.net.solutions.ka2a.models.GetTaskResponse
import sk.ai.net.solutions.ka2a.models.a2aJson

import sk.ai.net.solutions.ka2a.models.CancelTaskRequest
import sk.ai.net.solutions.ka2a.models.CancelTaskResponse
import sk.ai.net.solutions.ka2a.models.GetTaskPushNotificationRequest
import sk.ai.net.solutions.ka2a.models.GetTaskPushNotificationResponse
import sk.ai.net.solutions.ka2a.models.Message
import sk.ai.net.solutions.ka2a.models.PushNotificationConfig
import sk.ai.net.solutions.ka2a.models.SendTaskRequest
import sk.ai.net.solutions.ka2a.models.SendTaskResponse
import sk.ai.net.solutions.ka2a.models.SendTaskStreamingRequest
import sk.ai.net.solutions.ka2a.models.SendTaskStreamingResponse
import sk.ai.net.solutions.ka2a.models.SetTaskPushNotificationRequest
import sk.ai.net.solutions.ka2a.models.SetTaskPushNotificationResponse
import sk.ai.net.solutions.ka2a.models.StringValue
import sk.ai.net.solutions.ka2a.models.TaskArtifactUpdateEvent
import sk.ai.net.solutions.ka2a.models.TaskIdParams
import sk.ai.net.solutions.ka2a.models.TaskPushNotificationConfig
import sk.ai.net.solutions.ka2a.models.TaskQueryParams
import sk.ai.net.solutions.ka2a.models.TaskResubscriptionRequest
import sk.ai.net.solutions.ka2a.models.TaskSendParams
import sk.ai.net.solutions.ka2a.models.TaskStatusUpdateEvent
import sk.ai.net.solutions.ka2a.models.toJson
import kotlin.uuid.Uuid


/**
 * A2AClient implements an Agent-to-Agent communication client based on the A2A protocol.
 *
 * This client provides methods for interacting with an A2A server, including retrieving
 * the agent's metadata (agent card), sending and retrieving tasks, canceling tasks,
 * and managing push notification configurations.
 */
class A2AClient(
    baseUrl: String,
    endpoint: String = "/",
    httpClient: HttpClient? = null,
) : Closeable {

    /**
     * The HTTP client used for making requests to the server.
     */
    private val client: HttpClient = httpClient ?: HttpClient {
        defaultRequest {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }
        /*
        HttpClientConfig.install(SSE) {
            SSEConfig.maxReconnectionAttempts = 5
        }
        HttpClientConfig.install(HttpTimeout) {
            HttpTimeoutConfig.requestTimeoutMillis = 30000
            HttpTimeoutConfig.connectTimeoutMillis = 15000
            HttpTimeoutConfig.socketTimeoutMillis = 60000
        }
        
         */
    }

    /**
     * The full URL for the API endpoint.
     */
    private val apiUrl = "$baseUrl$endpoint"

    /**
     * The URL for retrieving the agent card.
     */
    private val agentCardUrl = "$baseUrl/.well-known/agent.json"

    /**
     * Retrieves the agent card from the server.
     *
     * @return The agent card containing metadata about the agent.
     * @throws Exception if the request fails or the response cannot be parsed.
     */
    suspend fun getAgentCard(): AgentCard {
        val response = client.get(agentCardUrl)
        if (response.status != HttpStatusCode.Companion.OK) {
            throw ServerException("Failed to get agent card: ${response.status}!")
        }
        return a2aJson.decodeFromString(response.bodyAsText())
    }

    /**
     * Retrieves a task by its ID.
     *
     * @param taskId The ID of the task to retrieve.
     * @param historyLength The maximum number of history entries to include in the response.
     * @param requestId A unique identifier for this request.
     * @return The response containing the requested task or an error if the task is not found.
     * @throws Exception if the request fails or the response cannot be parsed.
     */
    suspend fun getTask(
        taskId: String,
        historyLength: Int = 10,
        requestId: String = generateRequestId(),
    ): GetTaskResponse {
        val request = GetTaskRequest(
            id = StringValue(requestId),
            params = TaskQueryParams(id = taskId, historyLength = historyLength),
        )

        val response = client.post(apiUrl) {
            setBody(request.toJson())
        }

        if (response.status != HttpStatusCode.Companion.OK) {
            throw ServerException("Failed to get task: ${response.status}")
        }

        return a2aJson.decodeFromString(response.bodyAsText())
    }

    /**
     * Creates or updates a task.
     *
     * @param taskId The ID of the task to create or update.
     * @param sessionId The session ID for this task.
     * @param message The message to include in the task.
     * @param historyLength The maximum number of history entries to include in the response.
     * @param requestId A unique identifier for this request.
     * @return The response containing the created or updated task.
     * @throws Exception if the request fails or the response cannot be parsed.
     */
    suspend fun sendTask(
        message: Message,
        taskId: String = "task::${ UuidUtil.randomUuid()}",
        sessionId: String = "session::${UuidUtil.randomUuid()}",
        historyLength: Int = 10,
        requestId: String = generateRequestId(),
    ): SendTaskResponse {
        val request = SendTaskRequest(
            id = StringValue(requestId),
            params = TaskSendParams(
                id = taskId,
                sessionId = sessionId,
                message = message,
                historyLength = historyLength,
            ),
        )

        val response = client.post(apiUrl) {
            setBody(request.toJson())
        }

        if (response.status != HttpStatusCode.Companion.OK) {
            throw ServerException("Failed to send task: ${response.status}")
        }

        return a2aJson.decodeFromString(response.bodyAsText())
    }

    /**
     * Subscribes to streaming updates for a task.
     *
     * @param taskId The ID of the task to subscribe to.
     * @param sessionId The session ID for this task.
     * @param message The message to include in the task.
     * @param historyLength The maximum number of history entries to include in the response.
     * @param requestId A unique identifier for this request.
     * @return A flow of streaming responses with task updates.
     * @throws Exception if the request fails or the response cannot be parsed.
     */
    fun sendTaskStreaming(
        taskId: String,
        sessionId: String,
        message: Message,
        historyLength: Int = 10,
        requestId: String = generateRequestId(),
    ): Flow<SendTaskStreamingResponse> = flow {
        val request = SendTaskStreamingRequest(
            id = StringValue(requestId),
            params = TaskSendParams(
                id = taskId,
                sessionId = sessionId,
                message = message,
                historyLength = historyLength,
            ),
        )
        client.sse(request = {
            url(apiUrl)
            setBody(request.toJson())
        }) {
            incoming.collect { serverSentEvent ->
                val responseBody = serverSentEvent.data ?: return@collect
                val response = a2aJson.decodeFromString<SendTaskStreamingResponse>(responseBody)
                emit(response)
                when (val result = response.result) {
                    is TaskArtifactUpdateEvent -> {}

                    is TaskStatusUpdateEvent -> {
                        if (result.final) cancel()
                    }

                    null -> {}
                }
            }
        }
    }

    /**
     * Attempts to cancel a task.
     *
     * @param taskId The ID of the task to cancel.
     * @param requestId A unique identifier for this request.
     * @return The response indicating success or failure of the cancellation.
     * @throws Exception if the request fails or the response cannot be parsed.
     */
    suspend fun cancelTask(taskId: String, requestId: String = generateRequestId()): CancelTaskResponse {
        val request = CancelTaskRequest(
            id = StringValue(requestId),
            params = TaskIdParams(id = taskId),
        )

        val response = client.post(apiUrl) {
            setBody(request.toJson())
        }

        if (response.status != HttpStatusCode.Companion.OK) {
            throw Exception("Failed to cancel task: ${response.status}")
        }

        return a2aJson.decodeFromString(response.bodyAsText())
    }

    /**
     * Sets push notification configuration for a task.
     *
     * @param taskId The ID of the task to configure.
     * @param config The push notification configuration.
     * @param requestId A unique identifier for this request.
     * @return The response indicating success or failure of the operation.
     * @throws Exception if the request fails or the response cannot be parsed.
     */
    suspend fun setTaskPushNotification(
        taskId: String,
        config: PushNotificationConfig,
        requestId: String = generateRequestId(),
    ): SetTaskPushNotificationResponse {
        val request = SetTaskPushNotificationRequest(
            id = StringValue(requestId),
            params = TaskPushNotificationConfig(
                id = taskId,
                pushNotificationConfig = config,
            ),
        )

        val response = client.post(apiUrl) {
            setBody(request.toJson())
        }

        if (response.status != HttpStatusCode.Companion.OK) {
            throw Exception("Failed to set task push notification: ${response.status}")
        }

        return a2aJson.decodeFromString(response.bodyAsText())
    }

    /**
     * Retrieves the push notification configuration for a task.
     *
     * @param taskId The ID of the task.
     * @param requestId A unique identifier for this request.
     * @return The response containing the push notification configuration or an error if not found.
     * @throws Exception if the request fails or the response cannot be parsed.
     */
    suspend fun getTaskPushNotification(
        taskId: String,
        requestId: String = generateRequestId(),
    ): GetTaskPushNotificationResponse {
        val request = GetTaskPushNotificationRequest(
            id = StringValue(requestId),
            params = TaskIdParams(id = taskId),
        )

        val response = client.post(apiUrl) {
            setBody(request.toJson())
        }

        if (response.status != HttpStatusCode.Companion.OK) {
            throw Exception("Failed to get task push notification: ${response.status}")
        }

        return a2aJson.decodeFromString(response.bodyAsText())
    }

    /**
     * Resubscribes to a task to receive streaming updates.
     *
     * @param taskId The ID of the task to resubscribe to.
     * @param requestId A unique identifier for this request.
     * @return A flow of streaming responses with task updates.
     * @throws Exception if the request fails or the response cannot be parsed.
     */
    fun resubscribeToTask(
        taskId: String,
        requestId: String = generateRequestId(),
    ): Flow<SendTaskStreamingResponse> = flow {
        val request = TaskResubscriptionRequest(id = StringValue(requestId), params = TaskQueryParams(id = taskId))

        client.sse(request = {
            url(apiUrl)
            setBody(request.toJson())
        }) {
            incoming.collect { serverSentEvent ->
                val responseBody = serverSentEvent.data ?: return@collect
                val response = a2aJson.decodeFromString<SendTaskStreamingResponse>(responseBody)
                emit(response)
            }
        }
    }

    /**
     * Closes the HTTP client and releases resources.
     */
    override fun close() {
        client.close()
    }

    /**
     * Generates a unique request ID.
     *
     * @return A unique string ID.
     */
    private fun generateRequestId(): String {
        return "${UuidUtil.randomUuid()}"
    }
}