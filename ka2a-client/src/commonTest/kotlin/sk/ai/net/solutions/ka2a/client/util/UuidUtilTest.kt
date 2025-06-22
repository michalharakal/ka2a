package sk.ai.net.solutions.ka2a.client.util

import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UuidUtilTest {

    @Test
    fun testRandomUuid() {
        // Generate two UUIDs
        val uuid1 = UuidUtil.randomUuid()
        val uuid2 = UuidUtil.randomUuid()

        // Verify that the UUIDs are not null
        assertNotNull(uuid1)
        assertNotNull(uuid2)

        // Verify that the UUIDs are not empty
        assertTrue(uuid1.isNotEmpty())
        assertTrue(uuid2.isNotEmpty())

        // Verify that the UUIDs are different
        assertNotEquals(uuid1, uuid2)

        // Print the UUIDs for debugging
        println("UUID 1: $uuid1")
        println("UUID 2: $uuid2")
    }
}