// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0
package sk.ai.net.solutions.ka2a.models

import kotlinx.serialization.Serializable

/**
 * A2A Protocol Error types
 */
@Serializable
sealed class JsonRpcError {
    abstract val code: Int
    abstract val message: String
    abstract val data: Map<String, String>?
}

@Serializable
class JsonParseError : JsonRpcError() {
    override val code: Int = -32700
    override val message: String = "Invalid JSON payload"
    override val data: Map<String, String>? = null
}

@Serializable
class InvalidRequestError : JsonRpcError() {
    override val code: Int = -32600
    override val message: String = "Request payload validation error"
    override val data: Map<String, String>? = null
}

@Serializable
class MethodNotFoundError : JsonRpcError() {
    override val code: Int = -32601
    override val message: String = "Method not found"
    override val data: Map<String, String>? = null
}

@Serializable
class InvalidParamsError : JsonRpcError() {
    override val code: Int = -32602
    override val message: String = "Invalid parameters"
    override val data: Map<String, String>? = null
}

@Serializable
class InternalError : JsonRpcError() {
    override val code: Int = -32603
    override val message: String = "Internal error"
    override val data: Map<String, String>? = null
}

@Serializable
class TaskNotFoundError : JsonRpcError() {
    override val code: Int = -32001
    override val message: String = "Task not found"
    override val data: Map<String, String>? = null
}

@Serializable
class TaskNotCancelableError : JsonRpcError() {
    override val code: Int = -32002
    override val message: String = "Task cannot be canceled"
    override val data: Map<String, String>? = null
}

@Serializable
class PushNotificationNotSupportedError : JsonRpcError() {
    override val code: Int = -32003
    override val message: String = "Push Notification is not supported"
    override val data: Map<String, String>? = null
}

@Serializable
class UnsupportedOperationError : JsonRpcError() {
    override val code: Int = -32004
    override val message: String = "This operation is not supported"
    override val data: Map<String, String>? = null
}
