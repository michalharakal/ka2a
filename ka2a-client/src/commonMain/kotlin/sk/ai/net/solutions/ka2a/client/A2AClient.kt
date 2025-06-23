package sk.ai.net.solutions.ka2a.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import sk.ai.net.solutions.ka2a.client.util.UuidUtil

// Using model classes from ka2a-models module
import sk.ai.net.solutions.ka2a.models.A2ARequest
import sk.ai.net.solutions.ka2a.models.A2AResponse
import sk.ai.net.solutions.ka2a.models.Destination
import sk.ai.net.solutions.ka2a.models.Origin
import sk.ai.net.solutions.ka2a.models.Payload
import sk.ai.net.solutions.ka2a.models.Status
import sk.ai.net.solutions.ka2a.models.A2AStatusCode
import sk.ai.net.solutions.ka2a.models.JsonRpcRequest
import sk.ai.net.solutions.ka2a.models.JsonRpcResponse
import sk.ai.net.solutions.ka2a.models.JsonRpcError
import sk.ai.net.solutions.ka2a.models.toJsonRpcRequest
import sk.ai.net.solutions.ka2a.models.toA2AResponse

/**
 * Interface for A2A client operations.
 */
interface A2AClient {
    /**
     * Checks if the server at the specified URL is available.
     *
     * @param serverUrl The URL of the server to check.
     * @return A Flow emitting a boolean indicating if the server is available.
     */
    fun checkServerAvailability(serverUrl: String): Flow<Boolean>

    /**
     * Sends an A2A request to the specified destination.
     *
     * @param request The A2A request to send.
     * @return A Flow emitting the A2A response.
     */
    fun sendRequest(request: A2ARequest): Flow<A2AResponse>

    /**
     * Creates a new A2A request with the specified parameters.
     *
     * @param action The action to perform.
     * @param destination The destination of the request.
     * @param payload Optional payload to include in the request.
     * @param metadata Optional metadata to include in the request.
     * @return A new A2A request.
     */
    fun createRequest(
        action: String,
        destination: Destination,
        payload: Payload? = null,
        metadata: Map<String, String>? = null
    ): A2ARequest

    /**
     * Performs a ping operation to check if the destination is available.
     *
     * @param destination The destination to ping.
     * @return A Flow emitting the A2A response.
     */
    fun ping(destination: Destination): Flow<A2AResponse>

    /**
     * Discovers the capabilities of the destination.
     *
     * @param destination The destination to discover.
     * @return A Flow emitting the A2A response.
     */
    fun discover(destination: Destination): Flow<A2AResponse>

    /**
     * Authenticates with the destination.
     *
     * @param destination The destination to authenticate with.
     * @param authPayload The authentication payload.
     * @return A Flow emitting the A2A response.
     */
    fun authenticate(destination: Destination, authPayload: Payload): Flow<A2AResponse>

    /**
     * Authorizes an operation with the destination.
     *
     * @param destination The destination to authorize with.
     * @param authPayload The authorization payload.
     * @return A Flow emitting the A2A response.
     */
    fun authorize(destination: Destination, authPayload: Payload): Flow<A2AResponse>

    /**
     * Transfers data to the destination.
     *
     * @param destination The destination to transfer data to.
     * @param transferPayload The transfer payload.
     * @return A Flow emitting the A2A response.
     */
    fun transfer(destination: Destination, transferPayload: Payload): Flow<A2AResponse>

    /**
     * Performs a custom action with the destination.
     *
     * @param destination The destination to perform the action with.
     * @param action The custom action to perform.
     * @param payload The payload for the custom action.
     * @return A Flow emitting the A2A response.
     */
    fun customAction(destination: Destination, action: String, payload: Payload): Flow<A2AResponse>
}

/**
 * Default implementation of the A2AClient interface using Ktor for HTTP communication.
 *
 * @param appId The ID of the application using this client.
 * @param appType The type of the application using this client.
 * @param appUrl Optional URL of the application using this client.
 * @param httpClient Optional custom HTTP client to use for communication.
 */
class DefaultA2AClient(
    private val appId: String,
    private val appType: String,
    private val appUrl: String? = null,
    private val httpClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }
    }
) : A2AClient {

    companion object {
        const val A2A_VERSION = "1.0"
    }

    private val origin = Origin(id = appId, type = appType, url = appUrl)

    override fun checkServerAvailability(serverUrl: String): Flow<Boolean> = flow {
        try {
            // Try to access the agent card endpoint to check if the server is available
            val agentCardUrl = if (serverUrl.endsWith("/")) {
                serverUrl + ".well-known/agent-card"
            } else {
                serverUrl + "/.well-known/agent-card"
            }

            val response: HttpResponse = httpClient.get(agentCardUrl)
            emit(response.status.value in 200..299)
        } catch (e: Exception) {
            // If an exception occurs, the server is not available
            emit(false)
        }
    }

    /**
     * Sends an A2A request to the specified destination and handles various response formats.
     * 
     * This method wraps the A2A request in a JSON-RPC 2.0 envelope and handles three types of responses:
     * 1. Standard JSON-RPC responses with a result field containing an A2A response
     * 2. JSON-RPC error responses with jsonrpc, error.code, and error.message fields
     * 3. Other HTTP error responses (like Spring Boot error responses)
     *
     * For JSON-RPC errors, it maps the error codes to A2A status codes:
     * - -32600 (Invalid Request) -> BAD_REQUEST (400)
     * - -32601 (Method not found) -> NOT_FOUND (404)
     * - -32602 (Invalid params) -> BAD_REQUEST (400)
     * - -32603 (Internal error) -> INTERNAL_ERROR (500)
     *
     * @param request The A2A request to send.
     * @return A Flow emitting the A2A response.
     */
    override fun sendRequest(request: A2ARequest): Flow<A2AResponse> = flow {
        try {
            val baseUrl = request.destination.url
                ?: throw IllegalArgumentException("Destination URL is required")

            // Ensure the URL ends with the /a2a endpoint
            val destinationUrl = if (baseUrl.endsWith("/a2a")) {
                baseUrl
            } else {
                if (baseUrl.endsWith("/")) {
                    baseUrl + "a2a"
                } else {
                    "$baseUrl/a2a"
                }
            }

            // Convert A2ARequest to JsonRpcRequest
            val jsonRpcRequest = request.toJsonRpcRequest()

            // Log the request object
            println("Sending JSON-RPC request object: $jsonRpcRequest")

            // Log the serialized JSON for better debugging
            val jsonString = Json { 
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            }.encodeToString(JsonRpcRequest.serializer(), jsonRpcRequest)
            println("Sending JSON-RPC request JSON: $jsonString")

            println("Sending request to URL: $destinationUrl")

            val httpResponse = httpClient.post(destinationUrl) {
                contentType(ContentType.Application.Json)
                setBody(jsonRpcRequest)
                println("Request content type: ${ContentType.Application.Json}")
            }

            // Get the raw response body first for better error handling
            val responseText = httpResponse.bodyAsText()
            println("Received response status: ${httpResponse.status}")
            println("Received response headers: ${httpResponse.headers}")
            println("Received response body: $responseText")

            try {
                // Try to parse as JsonRpcResponse
                val jsonRpcResponse = Json.decodeFromString<JsonRpcResponse>(responseText)
                val a2aResponse = jsonRpcResponse.toA2AResponse(request.id)
                emit(a2aResponse)
            } catch (e: Exception) {
                // If parsing fails, it might be a standard HTTP error response or a JSON-RPC error response

                // First, check if it's a JSON-RPC error response
                if (responseText.contains("\"jsonrpc\"") && responseText.contains("\"error\"")) {
                    println("Detected JSON-RPC error response: $responseText")
                    try {
                        // Parse the JSON-RPC error response
                        val jsonRpcResponse = Json.parseToJsonElement(responseText).jsonObject
                        println("Parsed JSON-RPC response: $jsonRpcResponse")

                        // Check if jsonrpc version is present and valid
                        val jsonrpcVersion = jsonRpcResponse["jsonrpc"]?.jsonPrimitive?.content
                        println("JSON-RPC version in response: $jsonrpcVersion")

                        val errorObj = jsonRpcResponse["error"]?.jsonObject
                        println("Error object in response: $errorObj")

                        if (errorObj != null) {
                            val errorCode = errorObj["code"]?.jsonPrimitive?.content?.toIntOrNull() ?: -1
                            val errorMessage = errorObj["message"]?.jsonPrimitive?.content ?: "Unknown JSON-RPC error"
                            val errorData = errorObj["data"]?.toString()

                            println("Extracted JSON-RPC error: code=$errorCode, message=$errorMessage, data=$errorData")

                            // Map JSON-RPC error code to A2A status code
                            val statusCode = when (errorCode) {
                                -32600 -> A2AStatusCode.BAD_REQUEST.code // Invalid Request
                                -32601 -> A2AStatusCode.NOT_FOUND.code   // Method not found
                                -32602 -> A2AStatusCode.BAD_REQUEST.code // Invalid params
                                -32603 -> A2AStatusCode.INTERNAL_ERROR.code // Internal error
                                else -> A2AStatusCode.INTERNAL_ERROR.code
                            }

                            println("Mapped JSON-RPC error code $errorCode to A2A status code $statusCode")

                            val a2aResponse = A2AResponse(
                                version = A2A_VERSION,
                                requestId = request.id,
                                status = Status(
                                    code = statusCode,
                                    message = "JSON-RPC Error: $errorMessage (code: $errorCode)"
                                )
                            )

                            println("Created A2AResponse from JSON-RPC error: $a2aResponse")
                            emit(a2aResponse)
                            return@flow
                        }
                    } catch (jsonEx: Exception) {
                        // If JSON parsing fails, fall back to standard error handling
                        println("Failed to parse JSON-RPC error response: ${jsonEx.message}")
                    }
                }

                // If it's not a JSON-RPC error or JSON parsing failed, handle other non-A2A response formats
                // Include the raw response in the error message for better debugging
                val errorMessage = "Error parsing response: ${e.message}. JSON input: $responseText"

                // Try to extract status code from the exception message or response text
                val statusCode = when {
                    // Check HTTP status code first
                    httpResponse.status.value == 404 -> A2AStatusCode.NOT_FOUND.code
                    httpResponse.status.value == 500 -> A2AStatusCode.INTERNAL_ERROR.code
                    httpResponse.status.value == 400 -> A2AStatusCode.BAD_REQUEST.code
                    httpResponse.status.value == 401 -> A2AStatusCode.UNAUTHORIZED.code
                    httpResponse.status.value == 403 -> A2AStatusCode.FORBIDDEN.code

                    // Then check error message
                    e.message?.contains("404") == true -> A2AStatusCode.NOT_FOUND.code
                    e.message?.contains("500") == true -> A2AStatusCode.INTERNAL_ERROR.code
                    e.message?.contains("400") == true -> A2AStatusCode.BAD_REQUEST.code
                    e.message?.contains("401") == true -> A2AStatusCode.UNAUTHORIZED.code
                    e.message?.contains("403") == true -> A2AStatusCode.FORBIDDEN.code

                    // Finally check response text
                    responseText.contains("\"status\":404") -> A2AStatusCode.NOT_FOUND.code
                    responseText.contains("\"status\":500") -> A2AStatusCode.INTERNAL_ERROR.code
                    responseText.contains("\"status\":400") -> A2AStatusCode.BAD_REQUEST.code
                    responseText.contains("\"status\":401") -> A2AStatusCode.UNAUTHORIZED.code
                    responseText.contains("\"status\":403") -> A2AStatusCode.FORBIDDEN.code

                    else -> A2AStatusCode.INTERNAL_ERROR.code
                }

                emit(
                    A2AResponse(
                        version = A2A_VERSION,
                        requestId = request.id,
                        status = Status(
                            code = statusCode,
                            message = errorMessage
                        )
                    )
                )
            }
        } catch (e: Exception) {
            // Emit an error response for any other exceptions
            emit(
                A2AResponse(
                    version = A2A_VERSION,
                    requestId = request.id,
                    status = Status(
                        code = A2AStatusCode.INTERNAL_ERROR.code,
                        message = "Error: ${e.message}"
                    )
                )
            )
        }
    }

    override fun createRequest(
        action: String,
        destination: Destination,
        payload: Payload?,
        metadata: Map<String, String>?
    ): A2ARequest {
        return A2ARequest(
            version = A2A_VERSION,
            id = UuidUtil.randomUuid(),
            action = action,
            origin = origin,
            destination = destination,
            payload = payload,
            metadata = metadata
        )
    }

    override fun ping(destination: Destination): Flow<A2AResponse> {
        val request = createRequest(
            action = "ping",
            destination = destination
        )
        return sendRequest(request)
    }

    override fun discover(destination: Destination): Flow<A2AResponse> {
        val request = createRequest(
            action = "discover",
            destination = destination
        )
        return sendRequest(request)
    }

    override fun authenticate(destination: Destination, authPayload: Payload): Flow<A2AResponse> {
        val request = createRequest(
            action = "authenticate",
            destination = destination,
            payload = authPayload
        )
        return sendRequest(request)
    }

    override fun authorize(destination: Destination, authPayload: Payload): Flow<A2AResponse> {
        val request = createRequest(
            action = "authorize",
            destination = destination,
            payload = authPayload
        )
        return sendRequest(request)
    }

    override fun transfer(destination: Destination, transferPayload: Payload): Flow<A2AResponse> {
        val request = createRequest(
            action = "transfer",
            destination = destination,
            payload = transferPayload
        )
        return sendRequest(request)
    }

    override fun customAction(
        destination: Destination,
        action: String,
        payload: Payload
    ): Flow<A2AResponse> {
        val request = createRequest(
            action = action,
            destination = destination,
            payload = payload
        )
        return sendRequest(request)
    }
}
