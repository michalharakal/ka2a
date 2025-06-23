// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0
package sk.ai.net.solutions.ka2a.models

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

/**
 * Converter class for deserializing JSON strings to A2ARequest objects.
 */
class RequestConverter {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        coerceInputValues = true
        explicitNulls = false
        serializersModule = SerializersModule {
            polymorphicDefaultDeserializer(JsonRpcRequest::class) { UnknownMethodRequest.serializer() }
        }
    }

    /**
     * Converts a JSON string to an A2ARequest object.
     *
     * @param jsonString The JSON string to convert
     * @return The deserialized A2ARequest object
     * @throws IllegalArgumentException if the JSON is invalid
     */
    fun fromJson(jsonString: String): JsonRpcRequest {
        return json.decodeFromString<JsonRpcRequest>(jsonString)
    }
}
