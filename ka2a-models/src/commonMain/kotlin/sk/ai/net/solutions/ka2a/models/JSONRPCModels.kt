// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0
package sk.ai.net.solutions.ka2a.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * JSONRPCRequest represents a JSON-RPC request object base structure.
 * This class provides a more direct mapping to the Java implementation while
 * maintaining Kotlin's type safety.
 *
 * @property id The request identifier. Can be a string, number, or null.
 * @property jsonrpc The JSON-RPC version. Must be "2.0".
 * @property method The name of the method to be invoked.
 * @property params The parameters for the method.
 */
@Serializable
data class JSONRPCRequest(
    val id: StringOrInt? = null,
    val jsonrpc: String = "2.0",
    val method: String,
    val params: JsonElement? = null,
)

/**
 * JSONRPCResponse represents a JSON-RPC response object.
 * This class provides a more direct mapping to the Java implementation while
 * maintaining Kotlin's type safety.
 *
 * @property id The request identifier. Can be a string, number, or null.
 * @property jsonrpc The JSON-RPC version. Must be "2.0".
 * @property result The result of the method invocation. Required on success.
 * @property error The error object if an error occurred during the request.
 */
@Serializable
data class JSONRPCResponse(
    val id: StringOrInt? = null,
    val jsonrpc: String = "2.0",
    val result: JsonElement? = null,
    val error: JSONRPCError? = null,
)

/**
 * JSONRPCError represents a JSON-RPC error object.
 * This class provides a more direct mapping to the Java implementation while
 * maintaining Kotlin's type safety.
 *
 * @property code A number indicating the error type that occurred.
 * @property message A string providing a short description of the error.
 * @property data Optional additional data about the error.
 */
@Serializable
data class JSONRPCError(
    val code: Int,
    val message: String,
    val data: JsonElement? = null,
)

/**
 * Converts a JsonRpcRequest to a JSONRPCRequest.
 * This utility function helps maintain compatibility between the two approaches.
 *
 * @return A new JSONRPCRequest instance
 */
fun JsonRpcRequest.toJSONRPCRequest(): JSONRPCRequest {
    return JSONRPCRequest(
        id = this.id,
        jsonrpc = this.jsonrpc,
        method = when (this) {
            is SendTaskRequest -> "tasks/send"
            is GetTaskRequest -> "tasks/get"
            is CancelTaskRequest -> "tasks/cancel"
            is SetTaskPushNotificationRequest -> "tasks/pushNotification/set"
            is GetTaskPushNotificationRequest -> "tasks/pushNotification/get"
            is TaskResubscriptionRequest -> "tasks/resubscribe"
            is SendTaskStreamingRequest -> "tasks/sendSubscribe"
            else -> "unknown"
        },
        params = null // This would need to be implemented with a proper JSON serialization
    )
}

/**
 * Converts a JSONRPCRequest to a JsonRpcRequest.
 * This utility function helps maintain compatibility between the two approaches.
 *
 * @return A new JsonRpcRequest instance
 */
fun JSONRPCRequest.toJsonRpcRequest(): JsonRpcRequest {
    // This would need to be implemented with proper deserialization of params
    return UnknownMethodRequest(jsonrpc = this.jsonrpc, id = this.id)
}

/**
 * Converts a JsonRpcResponse to a JSONRPCResponse.
 * This utility function helps maintain compatibility between the two approaches.
 *
 * @return A new JSONRPCResponse instance
 */
fun JsonRpcResponse.toJSONRPCResponse(): JSONRPCResponse {
    return JSONRPCResponse(
        id = this.id,
        jsonrpc = this.jsonrpc,
        result = null, // This would need to be implemented with a proper JSON serialization
        error = this.error?.let { 
            JSONRPCError(
                code = it.code,
                message = it.message,
                data = null // This would need to be implemented with a proper JSON serialization
            )
        }
    )
}

/**
 * Converts a JSONRPCResponse to a JsonRpcResponse.
 * This utility function helps maintain compatibility between the two approaches.
 *
 * @return A new JsonRpcResponse instance
 */
fun JSONRPCResponse.toJsonRpcResponse(): JsonRpcResponse {
    // This would need to be implemented with proper deserialization of result
    return DefaultResponse(
        jsonrpc = this.jsonrpc,
        id = this.id,
        result = null,
        error = this.error?.let {
            // This would need to be implemented with proper mapping to JsonRpcError
            InternalError()
        }
    )
}
