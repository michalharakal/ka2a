package sk.ai.net.solutions.ka2a.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import sk.ai.net.solutions.ka2a.models.Destination
import sk.ai.net.solutions.ka2a.models.Payload

class A2AClientTest {

    @Test
    fun testCreateRequest() {
        // Create a client
        val client = DefaultA2AClient(
            appId = "test-app",
            appType = "test-type",
            appUrl = "https://example.com/test-app"
        )

        // Create a destination
        val destination = Destination(
            id = "test-destination",
            type = "test-type",
            url = "https://example.com/test-destination"
        )

        // Create a payload
        val payload = Payload(
            content = mapOf("key" to "value"),
            contentType = "application/json"
        )

        // Create a request
        val request = client.createRequest(
            action = "test-action",
            destination = destination,
            payload = payload,
            metadata = mapOf("meta-key" to "meta-value")
        )

        // Verify the request
        assertEquals(DefaultA2AClient.A2A_VERSION, request.version)
        assertNotNull(request.id)
        assertTrue(request.id.isNotEmpty())
        assertEquals("test-action", request.action)
        assertEquals("test-app", request.origin.id)
        assertEquals("test-type", request.origin.type)
        assertEquals("https://example.com/test-app", request.origin.url)
        assertEquals("test-destination", request.destination.id)
        assertEquals("test-type", request.destination.type)
        assertEquals("https://example.com/test-destination", request.destination.url)
        assertNotNull(request.payload)
        assertEquals("application/json", request.payload?.contentType)
        assertEquals("value", request.payload?.content?.get("key"))
        assertNotNull(request.metadata)
        assertEquals("meta-value", request.metadata?.get("meta-key"))
    }

    @Test
    fun testPingRequest() {
        // Create a client
        val client = DefaultA2AClient(
            appId = "test-app",
            appType = "test-type",
            appUrl = "https://example.com/test-app"
        )

        // Create a destination
        val destination = Destination(
            id = "test-destination",
            type = "test-type",
            url = "https://example.com/test-destination"
        )

        // Create a ping request using the ping method
        val pingFlow = client.ping(destination)

        // We can't easily test the flow without a mock HTTP client,
        // but we can verify that the flow is created
        assertNotNull(pingFlow)
    }
}
