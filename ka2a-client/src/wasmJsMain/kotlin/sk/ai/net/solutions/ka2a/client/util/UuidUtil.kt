package sk.ai.net.solutions.ka2a.client.util

import kotlin.random.Random

/**
 * WebAssembly JS implementation of UuidUtil.
 */
actual object UuidUtil {
    /**
     * Generates a random UUID string for WebAssembly JS.
     *
     * @return A random UUID string.
     */
    actual fun randomUuid(): String {
        // Simple implementation that generates a UUID-like string using pure Kotlin
        val timestamp = "currentTimeMillis().toString()"
        val random = Random.nextInt(0, 1000000).toString()
        return "wasm-$timestamp-$random"
    }
}
