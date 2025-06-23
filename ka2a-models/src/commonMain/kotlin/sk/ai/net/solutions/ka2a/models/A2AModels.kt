// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0
package sk.ai.net.solutions.ka2a.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A2A Protocol Request model.
 */
@Serializable
data class A2ARequest(
    val version: String,
    val id: String,
    val action: String,
    val origin: Origin,
    val destination: Destination,
    val payload: Payload? = null,
    val metadata: Map<String, String>? = null
)

/**
 * A2A Protocol Response model.
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
 * Status model for A2A responses.
 */
@Serializable
data class Status(
    val code: Int,
    val message: String
)

/**
 * Origin model for A2A requests.
 */
@Serializable
data class Origin(
    val id: String,
    val type: String,
    val url: String? = null
)

/**
 * Destination model for A2A requests.
 */
@Serializable
data class Destination(
    val id: String? = null,
    val type: String? = null,
    val url: String? = null
)

/**
 * Payload model for A2A requests and responses.
 */
@Serializable
data class Payload(
    val content: Map<String, String>? = null,
    val contentType: String? = null
)

/**
 * Status codes for A2A responses.
 */
enum class A2AStatusCode(val code: Int) {
    SUCCESS(200),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    INTERNAL_ERROR(500)
}

/**
 * Converts an A2ARequest to a JsonRpcRequest for the tasks/send method.
 */
fun A2ARequest.toJsonRpcRequest(): JsonRpcRequest {
    return SendTaskRequest(
        jsonrpc = "2.0",
        id = StringValue(this.id),
        params = TaskSendParams(
            id = this.id,
            message = Message(
                role = "user",
                parts = listOf(TextPart(text = this.action)),
                metadata = this.metadata ?: emptyMap()
            ),
            metadata = mapOf(
                "action" to this.action,
                "origin_id" to this.origin.id,
                "origin_type" to this.origin.type,
                "destination_id" to (this.destination.id ?: ""),
                "destination_type" to (this.destination.type ?: ""),
                "destination_url" to (this.destination.url ?: "")
            ) + (this.metadata ?: emptyMap())
        )
    )
}

/**
 * Converts a JsonRpcResponse to an A2AResponse.
 */
fun JsonRpcResponse.toA2AResponse(requestId: String): A2AResponse {
    return when (this) {
        is SendTaskResponse -> {
            val error = this.error
            if (error != null) {
                A2AResponse(
                    version = "1.0",
                    requestId = requestId,
                    status = Status(
                        code = mapErrorCodeToStatusCode(error.code),
                        message = error.message
                    )
                )
            } else {
                val result = this.result
                if (result != null) {
                    A2AResponse(
                        version = "1.0",
                        requestId = requestId,
                        status = Status(
                            code = A2AStatusCode.SUCCESS.code,
                            message = "Success"
                        ),
                        payload = result.artifacts?.firstOrNull()?.let { artifact ->
                            val textPart = artifact.parts.firstOrNull() as? TextPart
                            if (textPart != null) {
                                Payload(
                                    content = mapOf("text" to textPart.text),
                                    contentType = "text/plain"
                                )
                            } else null
                        }
                    )
                } else {
                    A2AResponse(
                        version = "1.0",
                        requestId = requestId,
                        status = Status(
                            code = A2AStatusCode.SUCCESS.code,
                            message = "Success (empty result)"
                        )
                    )
                }
            }
        }
        else -> {
            A2AResponse(
                version = "1.0",
                requestId = requestId,
                status = Status(
                    code = A2AStatusCode.SUCCESS.code,
                    message = "Success"
                )
            )
        }
    }
}

/**
 * Maps a JSON-RPC error code to an A2A status code.
 */
private fun mapErrorCodeToStatusCode(errorCode: Int): Int {
    return when (errorCode) {
        -32600 -> A2AStatusCode.BAD_REQUEST.code // Invalid Request
        -32601 -> A2AStatusCode.NOT_FOUND.code   // Method not found
        -32602 -> A2AStatusCode.BAD_REQUEST.code // Invalid params
        -32603 -> A2AStatusCode.INTERNAL_ERROR.code // Internal error
        else -> A2AStatusCode.INTERNAL_ERROR.code
    }
}