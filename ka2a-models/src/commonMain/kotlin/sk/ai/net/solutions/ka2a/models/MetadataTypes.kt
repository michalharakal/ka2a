// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0
package sk.ai.net.solutions.ka2a.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Type alias for the simple string-to-string metadata map.
 * This is the most type-safe approach but may not be fully compatible with Java's Object values.
 */
typealias StringMetadata = Map<String, String>

/**
 * A more flexible metadata value that can represent different types.
 * This provides a balance between type safety and flexibility.
 *
 * @property stringValue Optional string value
 * @property numberValue Optional number value
 * @property booleanValue Optional boolean value
 * @property nullValue Whether this value is null
 */
@Serializable
data class MetadataValue(
    val stringValue: String? = null,
    val numberValue: Double? = null,
    val booleanValue: Boolean? = null,
    val nullValue: Boolean = false,
) {
    companion object {
        /**
         * Creates a MetadataValue from a string.
         */
        fun fromString(value: String): MetadataValue = MetadataValue(stringValue = value)

        /**
         * Creates a MetadataValue from a number.
         */
        fun fromNumber(value: Double): MetadataValue = MetadataValue(numberValue = value)

        /**
         * Creates a MetadataValue from a boolean.
         */
        fun fromBoolean(value: Boolean): MetadataValue = MetadataValue(booleanValue = value)

        /**
         * Creates a null MetadataValue.
         */
        fun nullValue(): MetadataValue = MetadataValue(nullValue = true)
    }

    /**
     * Gets the value as a string, or null if this value doesn't represent a string.
     */
    fun asStringOrNull(): String? = stringValue

    /**
     * Gets the value as a number, or null if this value doesn't represent a number.
     */
    fun asNumberOrNull(): Double? = numberValue

    /**
     * Gets the value as a boolean, or null if this value doesn't represent a boolean.
     */
    fun asBooleanOrNull(): Boolean? = booleanValue

    /**
     * Whether this value is null.
     */
    fun isNull(): Boolean = nullValue
}

/**
 * Type alias for a more flexible metadata map that can handle different value types.
 */
typealias FlexibleMetadata = Map<String, MetadataValue>

/**
 * Converts a StringMetadata to a FlexibleMetadata.
 */
fun StringMetadata.toFlexibleMetadata(): FlexibleMetadata {
    return mapValues { (_, value) -> MetadataValue.fromString(value) }
}

/**
 * Converts a FlexibleMetadata to a StringMetadata.
 * Note that this may lose information if the FlexibleMetadata contains non-string values.
 */
fun FlexibleMetadata.toStringMetadata(): StringMetadata {
    return mapNotNull { (key, value) ->
        value.asStringOrNull()?.let { stringValue -> key to stringValue }
    }.toMap()
}

/**
 * A fully flexible metadata value that can represent any JSON value.
 * This provides maximum flexibility but minimal type safety.
 *
 * @property value The JSON element representing the value
 */
@Serializable
data class JsonMetadataValue(
    val value: JsonElement,
)

/**
 * Type alias for a fully flexible metadata map that can handle any JSON value.
 */
typealias JsonMetadata = Map<String, JsonMetadataValue>