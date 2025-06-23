// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0
package sk.ai.net.solutions.ka2a.models

import kotlinx.serialization.Serializable
import sk.ai.net.solutions.ka2a.models.Task
import sk.ai.net.solutions.ka2a.models.TaskStreamingResult

/**
 * A2A Protocol Response types
 */
@Serializable
sealed class JsonRpcResponse {
    abstract val jsonrpc: String
    abstract val id: StringOrInt?
    abstract val result: Any?
    abstract val error: JsonRpcError?
}

@Serializable
data class SendTaskResponse(
    override val jsonrpc: String = "2.0",
    override val id: StringOrInt? = null,
    override val result: Task? = null,
    override val error: JsonRpcError? = null,
) : JsonRpcResponse()

@Serializable
data class GetTaskResponse(
    override val jsonrpc: String = "2.0",
    override val id: StringOrInt? = null,
    override val result: Task? = null,
    override val error: JsonRpcError? = null,
) : JsonRpcResponse()

@Serializable
data class CancelTaskResponse(
    override val jsonrpc: String = "2.0",
    override val id: StringOrInt? = null,
    override val result: Task? = null,
    override val error: JsonRpcError? = null,
) : JsonRpcResponse()

@Serializable
data class SetTaskPushNotificationResponse(
    override val jsonrpc: String = "2.0",
    override val id: StringOrInt? = null,
    override val result: TaskPushNotificationConfig? = null,
    override val error: JsonRpcError? = null,
) : JsonRpcResponse()

@Serializable
data class GetTaskPushNotificationResponse(
    override val jsonrpc: String = "2.0",
    override val id: StringOrInt? = null,
    override val result: TaskPushNotificationConfig? = null,
    override val error: JsonRpcError? = null,
) : JsonRpcResponse()

@Serializable
data class SendTaskStreamingResponse(
    override val jsonrpc: String = "2.0",
    override val id: StringOrInt? = null,
    override val result: TaskStreamingResult? = null,
    override val error: JsonRpcError? = null,
) : JsonRpcResponse()

@Serializable
data class ErrorResponse(
    override val jsonrpc: String = "2.0",
    override val id: StringOrInt? = null,
    override val result: String? = null,
    override val error: JsonRpcError? = null,
) : JsonRpcResponse()
