package sk.ai.net.solutions.ka2a.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import sk.ai.net.solutions.ka2a.client.model.A2ARequest
import sk.ai.net.solutions.ka2a.client.model.A2AResponse
import sk.ai.net.solutions.ka2a.client.model.A2AStatusCode
import sk.ai.net.solutions.ka2a.client.model.Destination
import sk.ai.net.solutions.ka2a.client.model.Origin
import sk.ai.net.solutions.ka2a.client.model.Payload
import sk.ai.net.solutions.ka2a.client.model.Status
import sk.ai.net.solutions.ka2a.client.util.UuidUtil

/**
 * Interface for A2A client operations.
 */
interface A2AClient {
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
            })
        }
    }
) : A2AClient {

    companion object {
        const val A2A_VERSION = "1.0"
    }

    private val origin = Origin(id = appId, type = appType, url = appUrl)

    override fun sendRequest(request: A2ARequest): Flow<A2AResponse> = flow {
        try {
            val destinationUrl = request.destination.url
                ?: throw IllegalArgumentException("Destination URL is required")

            val response = httpClient.post(destinationUrl) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<A2AResponse>()

            emit(response)
        } catch (e: Exception) {
            // Emit an error response
            emit(
                A2AResponse(
                    version = A2A_VERSION,
                    requestId = request.requestId,
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
            requestId = UuidUtil.randomUuid(),
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
