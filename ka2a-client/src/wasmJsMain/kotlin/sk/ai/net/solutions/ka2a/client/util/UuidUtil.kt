package sk.ai.net.solutions.ka2a.client.util

import kotlin.random.Random
import kotlin.time.TimeSource

/**
 * WebAssembly JS implementation of UuidUtil.
 */
actual object UuidUtil {
    // Use a monotonic time source to get a timestamp
    private val timeSource = TimeSource.Monotonic
    private val startMark = timeSource.markNow()

    /**
     * Generates a random UUID string for WebAssembly JS.
     *
     * @return A random UUID string.
     */
    actual fun randomUuid(): String {
        // Simple implementation that generates a UUID-like string using elapsed time and random number
        val elapsedMillis = startMark.elapsedNow().inWholeMilliseconds
        val random = Random.nextInt(0, 1000000).toString()
        return "wasm-$elapsedMillis-$random"
    }
}
