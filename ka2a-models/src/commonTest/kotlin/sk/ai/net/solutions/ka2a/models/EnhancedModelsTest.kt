// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0
package sk.ai.net.solutions.ka2a.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class EnhancedModelsTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testEnhancedMessageSerialization() {
        val message = Message(
            messageId = "msg-123",
            kind = "message",
            role = "assistant",
            parts = listOf(TextPart("Hello, world!")),
            contextId = "ctx-456",
            taskId = "task-789",
            referenceTaskIds = listOf("ref-task-1", "ref-task-2"),
            metadata = mapOf("key" to "value")
        )

        val serialized = json.encodeToString(message)
        val deserialized = json.decodeFromString<Message>(serialized)

        assertEquals(message.messageId, deserialized.messageId)
        assertEquals(message.kind, deserialized.kind)
        assertEquals(message.role, deserialized.role)
        assertEquals(message.contextId, deserialized.contextId)
        assertEquals(message.taskId, deserialized.taskId)
        assertEquals(message.referenceTaskIds, deserialized.referenceTaskIds)
        assertEquals(message.metadata, deserialized.metadata)
        assertEquals(1, deserialized.parts.size)
        assertEquals("Hello, world!", (deserialized.parts[0] as TextPart).text)
    }

    @Test
    fun testPartTypeDiscriminator() {
        val part = TextPart("Hello, world!")
        val serialized = json.encodeToString<Part>(part)

        // Verify that the serialized JSON uses "kind" as the discriminator
        assertTrue(serialized.contains("\"kind\":\"text\""))
    }

    @Test
    fun testAssistantMessageUtility() {
        val message = assistantMessage(
            text = "Hello, world!",
            messageId = "msg-123",
            contextId = "ctx-456",
            taskId = "task-789",
            referenceTaskIds = listOf("ref-task-1", "ref-task-2"),
            metadata = mapOf("key" to "value")
        )

        assertEquals("msg-123", message.messageId)
        assertEquals("message", message.kind)
        assertEquals("assistant", message.role)
        assertEquals("ctx-456", message.contextId)
        assertEquals("task-789", message.taskId)
        assertEquals(listOf("ref-task-1", "ref-task-2"), message.referenceTaskIds)
        assertEquals(mapOf("key" to "value"), message.metadata)
        assertEquals(1, message.parts.size)
        assertEquals("Hello, world!", (message.parts[0] as TextPart).text)
    }

    @Test
    fun testMessageConversionUtilities() {
        val original = Message(
            messageId = "msg-123",
            kind = "message",
            role = "assistant",
            parts = listOf(TextPart("Hello, world!")),
            contextId = "ctx-456",
            taskId = "task-789",
            referenceTaskIds = listOf("ref-task-1", "ref-task-2"),
            metadata = mapOf("key" to "value")
        )

        // Convert to simple message
        val simple = original.toSimpleMessage()
        assertEquals("assistant", simple.role)
        assertEquals(1, simple.parts.size)
        assertEquals("Hello, world!", (simple.parts[0] as TextPart).text)
        assertEquals(mapOf("key" to "value"), simple.metadata)
        assertNull(simple.messageId)
        assertNull(simple.contextId)
        assertNull(simple.taskId)
        assertNull(simple.referenceTaskIds)

        // Convert back to enhanced message
        val enhanced = simple.toEnhancedMessage(
            messageId = "msg-456",
            contextId = "ctx-789",
            taskId = "task-123",
            referenceTaskIds = listOf("ref-task-3", "ref-task-4")
        )
        assertEquals("msg-456", enhanced.messageId)
        assertEquals("message", enhanced.kind)
        assertEquals("assistant", enhanced.role)
        assertEquals("ctx-789", enhanced.contextId)
        assertEquals("task-123", enhanced.taskId)
        assertEquals(listOf("ref-task-3", "ref-task-4"), enhanced.referenceTaskIds)
        assertEquals(mapOf("key" to "value"), enhanced.metadata)
        assertEquals(1, enhanced.parts.size)
        assertEquals("Hello, world!", (enhanced.parts[0] as TextPart).text)
    }

    @Test
    fun testMetadataTypeOptions() {
        // StringMetadata
        val stringMetadata: StringMetadata = mapOf("key" to "value")
        assertEquals("value", stringMetadata["key"])

        // FlexibleMetadata
        val flexibleMetadata: FlexibleMetadata = mapOf(
            "stringKey" to MetadataValue.fromString("value"),
            "numberKey" to MetadataValue.fromNumber(42.0),
            "booleanKey" to MetadataValue.fromBoolean(true),
            "nullKey" to MetadataValue.nullValue()
        )
        assertEquals("value", flexibleMetadata["stringKey"]?.asStringOrNull())
        assertEquals(42.0, flexibleMetadata["numberKey"]?.asNumberOrNull())
        assertEquals(true, flexibleMetadata["booleanKey"]?.asBooleanOrNull())
        assertEquals(true, flexibleMetadata["nullKey"]?.isNull())

        // Convert between metadata types
        val convertedFlexibleMetadata = stringMetadata.toFlexibleMetadata()
        assertEquals("value", convertedFlexibleMetadata["key"]?.asStringOrNull())

        val convertedStringMetadata = flexibleMetadata.toStringMetadata()
        assertEquals("value", convertedStringMetadata["stringKey"])
        // Note: Non-string values are not included in the converted map
        assertNull(convertedStringMetadata["numberKey"])
        assertNull(convertedStringMetadata["booleanKey"])
        assertNull(convertedStringMetadata["nullKey"])
    }

    @Test
    fun testJSONRPCModels() {
        val request = JSONRPCRequest(
            id = StringValue("req-123"),
            jsonrpc = "2.0",
            method = "tasks/send",
            params = null
        )
        val serialized = json.encodeToString(request)
        val deserialized = json.decodeFromString<JSONRPCRequest>(serialized)

        assertEquals(StringValue("req-123"), deserialized.id)
        assertEquals("2.0", deserialized.jsonrpc)
        assertEquals("tasks/send", deserialized.method)
        assertNull(deserialized.params)

        val error = JSONRPCError(
            code = -32600,
            message = "Invalid Request",
            data = null
        )
        val response = JSONRPCResponse(
            id = StringValue("req-123"),
            jsonrpc = "2.0",
            result = null,
            error = error
        )
        val serializedResponse = json.encodeToString(response)
        val deserializedResponse = json.decodeFromString<JSONRPCResponse>(serializedResponse)

        assertEquals(StringValue("req-123"), deserializedResponse.id)
        assertEquals("2.0", deserializedResponse.jsonrpc)
        assertNull(deserializedResponse.result)
        assertNotNull(deserializedResponse.error)
        assertEquals(-32600, deserializedResponse.error?.code)
        assertEquals("Invalid Request", deserializedResponse.error?.message)
        assertNull(deserializedResponse.error?.data)
    }
}
