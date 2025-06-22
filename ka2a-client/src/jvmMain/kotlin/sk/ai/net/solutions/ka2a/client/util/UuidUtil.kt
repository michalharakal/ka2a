package sk.ai.net.solutions.ka2a.client.util

import java.util.UUID

/**
 * JVM implementation of UuidUtil.
 */
actual object UuidUtil {
    /**
     * Generates a random UUID string using java.util.UUID.
     *
     * @return A random UUID string.
     */
    actual fun randomUuid(): String = UUID.randomUUID().toString()
}