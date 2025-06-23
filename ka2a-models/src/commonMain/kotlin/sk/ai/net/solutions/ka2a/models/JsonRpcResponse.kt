// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0
package sk.ai.net.solutions.ka2a.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement
import sk.ai.net.solutions.ka2a.models.Task
import sk.ai.net.solutions.ka2a.models.TaskStreamingResult

/**
 * A2A Protocol Response types
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = JsonRpcResponseSerializer::class)
sealed class JsonRpcResponse {
    abstract val jsonrpc: String
    abstract val id: StringOrInt?
    abstract val result: Any?
    abstract val error: JsonRpcError?
}

@Serializable
@SerialName("tasks/send")
data class SendTaskResponse(
    override val jsonrpc: String = "2.0",
    override val id: StringOrInt? = null,
    override val result: Task? = null,
    override val error: JsonRpcError? = null,
) : JsonRpcResponse()

@Serializable
@SerialName("tasks/get")
data class GetTaskResponse(
    override val jsonrpc: String = "2.0",
    override val id: StringOrInt? = null,
    override val result: Task? = null,
    override val error: JsonRpcError? = null,
) : JsonRpcResponse()

@Serializable
@SerialName("tasks/cancel")
data class CancelTaskResponse(
    override val jsonrpc: String = "2.0",
    override val id: StringOrInt? = null,
    override val result: Task? = null,
    override val error: JsonRpcError? = null,
) : JsonRpcResponse()

@Serializable
@SerialName("tasks/pushNotification/set")
data class SetTaskPushNotificationResponse(
    override val jsonrpc: String = "2.0",
    override val id: StringOrInt? = null,
    override val result: TaskPushNotificationConfig? = null,
    override val error: JsonRpcError? = null,
) : JsonRpcResponse()

@Serializable
@SerialName("tasks/pushNotification/get")
data class GetTaskPushNotificationResponse(
    override val jsonrpc: String = "2.0",
    override val id: StringOrInt? = null,
    override val result: TaskPushNotificationConfig? = null,
    override val error: JsonRpcError? = null,
) : JsonRpcResponse()

@Serializable
@SerialName("tasks/sendSubscribe")
data class SendTaskStreamingResponse(
    override val jsonrpc: String = "2.0",
    override val id: StringOrInt? = null,
    override val result: TaskStreamingResult? = null,
    override val error: JsonRpcError? = null,
) : JsonRpcResponse()

@Serializable
@SerialName("error")
data class ErrorResponse(
    override val jsonrpc: String = "2.0",
    override val id: StringOrInt? = null,
    override val result: String? = null,
    override val error: JsonRpcError? = null,
) : JsonRpcResponse()

/**
 * Default response class used when the method field is missing.
 * This class can handle any result type using kotlinx.serialization.json.JsonElement.
 * The special @SerialName("") annotation makes this the default implementation when the discriminator is missing.
 */
@Serializable
@SerialName("")
data class DefaultResponse(
    override val jsonrpc: String = "2.0",
    override val id: StringOrInt? = null,
    override val result: kotlinx.serialization.json.JsonElement? = null,
    override val error: JsonRpcError? = null,
) : JsonRpcResponse()
