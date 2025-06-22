package sk.ai.net.solutions.ka2a.client.model

import kotlinx.serialization.Serializable

/**
 * A2A (App-to-App) protocol models based on the specification at
 * https://google-a2a.github.io/A2A/latest/specification/
 */

/**
 * Represents an A2A request.
 */
@Serializable
data class A2ARequest(
    val version: String,
    val requestId: String,
    val action: String,
    val origin: Origin,
    val destination: Destination,
    val payload: Payload? = null,
    val metadata: Map<String, String>? = null
)

/**
 * Represents an A2A response.
 */
@Serializable
data class A2AResponse(
    val version: String,
    val requestId: String,
    val status: Status,
    val payload: Payload? = null,
    val metadata: Map<String, String>? = null
)

/**
 * Represents the origin of an A2A request.
 */
@Serializable
data class Origin(
    val id: String,
    val type: String,
    val url: String? = null
)

/**
 * Represents the destination of an A2A request.
 */
@Serializable
data class Destination(
    val id: String,
    val type: String,
    val url: String? = null
)

/**
 * Represents the status of an A2A response.
 */
@Serializable
data class Status(
    val code: Int,
    val message: String? = null
)

/**
 * Represents the payload of an A2A request or response.
 * This is a generic container for any JSON-serializable content.
 */
@Serializable
data class Payload(
    val content: Map<String, String>,
    val contentType: String = "application/json"
)

/**
 * Enum representing standard A2A action types.
 */
enum class A2AActionType(val value: String) {
    PING("ping"),
    DISCOVER("discover"),
    AUTHENTICATE("authenticate"),
    AUTHORIZE("authorize"),
    TRANSFER("transfer"),
    CUSTOM("custom")
}

/**
 * Enum representing standard A2A status codes.
 */
enum class A2AStatusCode(val code: Int, val message: String) {
    SUCCESS(200, "Success"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    INTERNAL_ERROR(500, "Internal Server Error")
}