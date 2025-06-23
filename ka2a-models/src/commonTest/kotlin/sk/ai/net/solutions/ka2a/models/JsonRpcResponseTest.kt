// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0
package sk.ai.net.solutions.ka2a.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class JsonRpcResponseTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testDeserializeResponseWithoutMethod() {
        // This is similar to the JSON in the error message
        val jsonString = """
            {
                "id": "wasm-0-479093",
                "jsonrpc": "2.0",
                "result": {
                    "id": "wasm-0-479093",
                    "contextId": "46efa62e-58f6-4860-ab6a-05f3d717dcfc",
                    "kind": "task",
                    "status": {
                        "state": "failed",
                        "timestamp": "2025-06-23T20:58:01.954703Z"
                    },
                    "metadata": {
                        "action": "ping",
                        "origin_id": "ka2a-sample-app",
                        "origin_type": "web-client",
                        "destination_id": "a2a-server",
                        "destination_type": "server",
                        "destination_url": "http://localhost:8090"
                    }
                }
            }
        """.trimIndent()

        // This should not throw an exception
        val response = json.decodeFromString<JsonRpcResponse>(jsonString)

        // Verify that it was deserialized as a DefaultResponse
        assertTrue(response is DefaultResponse)
        assertEquals("wasm-0-479093", response.id?.toString())
        assertEquals("2.0", response.jsonrpc)
        assertNotNull(response.result)
    }

    @Test
    fun testDeserializeResponseWithMethod() {
        // Test with a method field
        val jsonString = """
            {
                "id": "wasm-0-479093",
                "jsonrpc": "2.0",
                "method": "tasks/send",
                "result": {
                    "id": "wasm-0-479093",
                    "contextId": "46efa62e-58f6-4860-ab6a-05f3d717dcfc",
                    "kind": "task",
                    "status": {
                        "state": "failed",
                        "timestamp": "2025-06-23T20:58:01.954703Z"
                    }
                }
            }
        """.trimIndent()

        // This should not throw an exception
        val response = json.decodeFromString<JsonRpcResponse>(jsonString)

        // Verify that it was deserialized as a SendTaskResponse
        assertTrue(response is SendTaskResponse)
        assertEquals("wasm-0-479093", response.id?.toString())
        assertEquals("2.0", response.jsonrpc)
        assertNotNull(response.result)
    }
}