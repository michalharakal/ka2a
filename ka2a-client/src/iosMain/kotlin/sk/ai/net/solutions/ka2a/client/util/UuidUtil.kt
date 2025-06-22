package sk.ai.net.solutions.ka2a.client.util

import platform.Foundation.NSUUID

/**
 * iOS implementation of UuidUtil.
 */
actual object UuidUtil {
    /**
     * Generates a random UUID string using NSUUID.
     *
     * @return A random UUID string.
     */
    actual fun randomUuid(): String = NSUUID().UUIDString
}