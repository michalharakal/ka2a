package sk.ai.net.solutions.ka2a.client.util

/**
 * Utility for generating UUIDs in a platform-independent way.
 */
expect object UuidUtil {
    /**
     * Generates a random UUID string.
     *
     * @return A random UUID string.
     */
    fun randomUuid(): String
}