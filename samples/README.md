# A2A Client Sample

This module demonstrates how to use the A2A client library to communicate with an A2A server. It's implemented as a WASM web application using Compose Multiplatform with Material 3 design.

## Features

- Connect to a locally running A2A server
- Execute A2A protocol operations (ping, discover)
- Translate text using the A2A server's translation service
- Display logs of all operations
- Show connectivity state

## Building and Running

### Prerequisites

- JDK 11 or later
- A running A2A server (see the main README for instructions)

### Running the Application

1. Start the A2A server:
   ```bash
   cd a2a-server-java
   ./gradlew bootRun
   ```

2. In a separate terminal, run the sample application:
   ```bash
   ./gradlew :samples:wasmJsBrowserDevelopmentRun
   ```

3. Open your browser at http://localhost:8080/ (the browser should open automatically)

## Usage

1. Enter the server URL (default: http://localhost:8090)
2. Click "Connect" to connect to the server
3. Once connected, you can:
   - Click "Ping" to ping the server
   - Click "Discover" to discover the server's capabilities
   - Enter text and select a language to translate
   - Click "Translate" to translate the text
4. All operations and their results will be displayed in the logs section

## Implementation Details

The sample application demonstrates:

- How to initialize and use the A2A client
- How to handle asynchronous responses using Kotlin coroutines and Flow
- How to build a user interface with Compose Multiplatform
- How to implement Material 3 design
- How to display logs and connectivity state

## Project Structure

- `src/commonMain/kotlin/sk/ai/net/solutions/ka2a/samples/model/` - Data models
- `src/commonMain/kotlin/sk/ai/net/solutions/ka2a/samples/viewmodel/` - View models
- `src/commonMain/kotlin/sk/ai/net/solutions/ka2a/samples/ui/` - UI components
- `src/wasmJsMain/kotlin/sk/ai/net/solutions/ka2a/samples/` - WASM-specific code
- `src/wasmJsMain/resources/` - Web resources