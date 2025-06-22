# KA2A Client

A Kotlin Multiplatform implementation of the A2A (App-to-App) client protocol based on the [A2A specification](https://google-a2a.github.io/A2A/latest/specification/).

## Features

- Fully multiplatform (Android, iOS, JVM, WebAssembly JS)
- Type-safe API for A2A communication
- Coroutine-based asynchronous API using Kotlin Flow
- Comprehensive error handling

## Installation

Add the module to your project by including it in your `settings.gradle.kts` file:

```kotlin
include(":ka2a-client")
```

Then add the dependency to your module's `build.gradle.kts` file:

```kotlin
dependencies {
    implementation(project(":ka2a-client"))
}
```

## Usage

### Creating a Client

```kotlin
// Create a client with your app's information
val client = DefaultA2AClient(
    appId = "your-app-id",
    appType = "your-app-type",
    appUrl = "https://your-app-url.com"
)
```

### Sending a Ping Request

```kotlin
// Create a destination
val destination = Destination(
    id = "destination-id",
    type = "destination-type",
    url = "https://destination-url.com"
)

// Send a ping request
client.ping(destination).collect { response ->
    println("Ping response: ${response.status.code} - ${response.status.message}")
}
```

### Sending a Custom Request

```kotlin
// Create a destination
val destination = Destination(
    id = "destination-id",
    type = "destination-type",
    url = "https://destination-url.com"
)

// Create a payload
val payload = Payload(
    content = mapOf("key" to "value"),
    contentType = "application/json"
)

// Send a custom request
client.customAction(
    destination = destination,
    action = "custom-action",
    payload = payload
).collect { response ->
    println("Custom action response: ${response.status.code} - ${response.status.message}")
    println("Payload: ${response.payload?.content}")
}
```

## API Reference

### A2AClient Interface

The `A2AClient` interface provides methods for sending A2A requests:

- `sendRequest(request: A2ARequest): Flow<A2AResponse>` - Sends an A2A request to the specified destination.
- `createRequest(action: String, destination: Destination, payload: Payload?, metadata: Map<String, String>?): A2ARequest` - Creates a new A2A request with the specified parameters.
- `ping(destination: Destination): Flow<A2AResponse>` - Performs a ping operation to check if the destination is available.
- `discover(destination: Destination): Flow<A2AResponse>` - Discovers the capabilities of the destination.
- `authenticate(destination: Destination, authPayload: Payload): Flow<A2AResponse>` - Authenticates with the destination.
- `authorize(destination: Destination, authPayload: Payload): Flow<A2AResponse>` - Authorizes an operation with the destination.
- `transfer(destination: Destination, transferPayload: Payload): Flow<A2AResponse>` - Transfers data to the destination.
- `customAction(destination: Destination, action: String, payload: Payload): Flow<A2AResponse>` - Performs a custom action with the destination.

### DefaultA2AClient Implementation

The `DefaultA2AClient` class provides a default implementation of the `A2AClient` interface using Ktor for HTTP communication.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
