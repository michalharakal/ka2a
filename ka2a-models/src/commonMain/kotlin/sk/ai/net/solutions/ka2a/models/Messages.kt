// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0
package sk.ai.net.solutions.ka2a.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
data class Artifact(
    val name: String? = null,
    val description: String? = null,
    val parts: List<Part>,
    val metadata: Map<String, String> = emptyMap(),
    val index: Int? = null,
    val append: Boolean? = null,
    val lastChunk: Boolean? = null,
)

fun textArtifact(text: String): Artifact {
    return Artifact(parts = listOf(TextPart(text)))
}

/**
 * Message represents a single message exchanged between user and agent
 *
 * @property messageId The identifier created by the message creator
 * @property kind The event type - message for Messages
 * @property role The message sender's role
 * @property parts The message content
 * @property contextId The context the message is associated with
 * @property taskId The identifier of task the message is related to
 * @property referenceTaskIds The list of tasks referenced as context by this message
 * @property metadata Extension metadata
 */
@Serializable
data class Message(
    val messageId: String? = null,
    val kind: String = "message",
    val role: String,
    val parts: List<Part>,
    val contextId: String? = null,
    val taskId: String? = null,
    val referenceTaskIds: List<String>? = null,
    val metadata: Map<String, String> = emptyMap(),
)

/**
 * Creates a message with the assistant role and a single text part.
 *
 * @param text The text content of the message
 * @param messageId Optional message identifier
 * @param contextId Optional context identifier
 * @param taskId Optional task identifier
 * @param referenceTaskIds Optional list of referenced task identifiers
 * @param metadata Optional metadata
 * @return A new Message instance with the assistant role
 */
fun assistantMessage(
    text: String,
    messageId: String? = null,
    contextId: String? = null,
    taskId: String? = null,
    referenceTaskIds: List<String>? = null,
    metadata: Map<String, String> = emptyMap()
): Message {
    return Message(
        messageId = messageId,
        kind = "message",
        role = "assistant",
        parts = listOf(TextPart(text)),
        contextId = contextId,
        taskId = taskId,
        referenceTaskIds = referenceTaskIds,
        metadata = metadata
    )
}

/**
 * Part represents a part of a message, which can be text, a file, or structured data.
 * The 'kind' property is used for type discrimination.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("kind")
sealed class Part {
    abstract val metadata: Map<String, String>
}

/**
 * TextPart represents a text segment within parts.
 */
@Serializable
@SerialName("text")
data class TextPart(
    val text: String,
    override val metadata: Map<String, String> = emptyMap(),
) : Part()

/**
 * FilePart represents a file attachment within parts.
 */
@Serializable
@SerialName("file")
data class FilePart(
    val file: FileData,
    override val metadata: Map<String, String> = emptyMap(),
) : Part()

/**
 * DataPart represents structured data within parts.
 */
@Serializable
@SerialName("data")
data class DataPart(
    val data: Map<String, String>,
    override val metadata: Map<String, String> = emptyMap(),
) : Part()

@Serializable
data class FileData(
    val name: String? = null,
    val mimeType: String? = null,
    val bytes: String? = null,
    val uri: String? = null,
)

@Serializable
data class Authentication(
    val schemes: List<String>,
    val credentials: String? = null,
)

@Serializable
data class Provider(
    val organization: String,
    val url: String,
)

@Serializable
data class Capabilities(
    val streaming: Boolean = false,
    val pushNotifications: Boolean = false,
    val stateTransitionHistory: Boolean = false,
)

/**
 * Converts an enhanced Message to a simple Message with only the essential fields.
 * This is useful for backward compatibility with code that expects the simpler Message structure.
 *
 * @return A new Message instance with only the essential fields
 */
fun Message.toSimpleMessage(): Message {
    return Message(
        role = this.role,
        parts = this.parts,
        metadata = this.metadata
    )
}

/**
 * Converts a simple Message to an enhanced Message with additional fields.
 * This is useful for upgrading existing code to use the enhanced Message structure.
 *
 * @param messageId Optional message identifier
 * @param contextId Optional context identifier
 * @param taskId Optional task identifier
 * @param referenceTaskIds Optional list of referenced task identifiers
 * @return A new Message instance with the additional fields
 */
fun Message.toEnhancedMessage(
    messageId: String? = null,
    contextId: String? = null,
    taskId: String? = null,
    referenceTaskIds: List<String>? = null
): Message {
    return Message(
        messageId = messageId,
        kind = "message",
        role = this.role,
        parts = this.parts,
        contextId = contextId,
        taskId = taskId,
        referenceTaskIds = referenceTaskIds,
        metadata = this.metadata
    )
}
