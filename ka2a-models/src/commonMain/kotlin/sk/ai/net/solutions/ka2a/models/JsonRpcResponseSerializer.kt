// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0
package sk.ai.net.solutions.ka2a.models

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Custom serializer for JsonRpcResponse that can handle missing method discriminators.
 * This serializer checks for the presence of a "method" field in the JSON and falls back
 * to DefaultResponse when it's missing.
 */
object JsonRpcResponseSerializer : JsonContentPolymorphicSerializer<JsonRpcResponse>(JsonRpcResponse::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out JsonRpcResponse> {
        val jsonObject = element.jsonObject
        
        // Check if the method field exists
        val methodField = jsonObject["method"]
        
        return if (methodField != null) {
            // If method field exists, use the standard polymorphic deserialization
            val methodValue = methodField.jsonPrimitive.content
            when (methodValue) {
                "tasks/send" -> SendTaskResponse.serializer()
                "tasks/get" -> GetTaskResponse.serializer()
                "tasks/cancel" -> CancelTaskResponse.serializer()
                "tasks/pushNotification/set" -> SetTaskPushNotificationResponse.serializer()
                "tasks/pushNotification/get" -> GetTaskPushNotificationResponse.serializer()
                "tasks/sendSubscribe" -> SendTaskStreamingResponse.serializer()
                "error" -> ErrorResponse.serializer()
                else -> DefaultResponse.serializer()
            }
        } else {
            // If method field is missing, use DefaultResponse
            DefaultResponse.serializer()
        }
    }
}