// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0
package sk.ai.net.solutions.ka2a.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

@Serializable(with = StringOrIntSerializer::class)
sealed class StringOrInt

@Serializable
data class IntValue(val value: Int?) : StringOrInt()

@Serializable
data class StringValue(val value: String?) : StringOrInt()

class StringOrIntSerializer : KSerializer<StringOrInt> {

    override val descriptor = buildClassSerialDescriptor(StringOrIntSerializer::class.toString()) {
        element<String>("int")
        element<Int>("string")
    }

    override fun serialize(encoder: Encoder, value: StringOrInt) {
        if (value is StringValue && value.value != null) {
            encoder.encodeString(value.value)
        } else if (value is IntValue && value.value != null) {
            encoder.encodeInt(value.value)
        } else {
            encoder.encodeNull()
        }
    }

    override fun deserialize(decoder: Decoder): StringOrInt {
        require(decoder is JsonDecoder)
        val element = decoder.decodeJsonElement()
        return if (element.jsonPrimitive.isString) {
            StringValue(element.jsonPrimitive.contentOrNull)
        } else {
            IntValue(element.jsonPrimitive.intOrNull)
        }
    }
}
