# KA2A Models

This module contains the Kotlin implementation of A2A (Agent-to-Agent) protocol models. The implementation has been enhanced to be closer to the Java specification while maintaining Kotlin's type safety.

## Key Features

- **Enhanced Message Class**: Added fields from the Java implementation (`messageId`, `kind`, `contextId`, `taskId`, `referenceTaskIds`) while maintaining backward compatibility.
- **Aligned Part Type Information**: Changed the discriminator from "type" to "kind" to match the Java implementation.
- **Enhanced Metadata Type Safety**: Provided multiple options for metadata handling with different levels of type safety and flexibility.
- **JSON-RPC Base Classes**: Added base classes that match the Java implementation while maintaining Kotlin's type safety.
- **Conversion Utilities**: Added utilities to convert between enhanced and simple models, maintaining compatibility with existing code.

## Usage Examples

### Enhanced Message Class

```kotlin
// Create a message with all fields
val message = Message(
    messageId = "msg-123",
    kind = "message",
    role = "assistant",
    parts = listOf(TextPart("Hello, world!")),
    contextId = "ctx-456",
    taskId = "task-789",
    referenceTaskIds = listOf("ref-task-1", "ref-task-2"),
    metadata = mapOf("key" to "value")
)

// Use the utility function for assistant messages
val assistantMessage = assistantMessage(
    text = "Hello, world!",
    messageId = "msg-123",
    contextId = "ctx-456",
    taskId = "task-789",
    referenceTaskIds = listOf("ref-task-1", "ref-task-2"),
    metadata = mapOf("key" to "value")
)
```

### Metadata Type Safety Options

```kotlin
// Option 1: Simple string-to-string metadata (most type-safe)
val stringMetadata: StringMetadata = mapOf("key" to "value")

// Option 2: Flexible metadata that can handle different value types
val flexibleMetadata: FlexibleMetadata = mapOf(
    "stringKey" to MetadataValue.fromString("value"),
    "numberKey" to MetadataValue.fromNumber(42.0),
    "booleanKey" to MetadataValue.fromBoolean(true),
    "nullKey" to MetadataValue.nullValue()
)

// Option 3: Fully flexible metadata that can handle any JSON value
val jsonMetadata: JsonMetadata = mapOf(
    "key" to JsonMetadataValue(JsonPrimitive("value"))
)

// Convert between metadata types
val convertedFlexibleMetadata = stringMetadata.toFlexibleMetadata()
val convertedStringMetadata = flexibleMetadata.toStringMetadata()
```

### JSON-RPC Base Classes

```kotlin
// Create a JSON-RPC request
val request = JSONRPCRequest(
    id = StringValue("req-123"),
    jsonrpc = "2.0",
    method = "tasks/send",
    params = null // Use JsonElement for params
)

// Create a JSON-RPC response
val response = JSONRPCResponse(
    id = StringValue("req-123"),
    jsonrpc = "2.0",
    result = null, // Use JsonElement for result
    error = JSONRPCError(
        code = -32600,
        message = "Invalid Request",
        data = null // Use JsonElement for data
    )
)
```

### Conversion Utilities

```kotlin
// Convert between enhanced and simple message models
val simpleMessage = message.toSimpleMessage()
val enhancedMessage = simpleMessage.toEnhancedMessage(
    messageId = "msg-123",
    contextId = "ctx-456",
    taskId = "task-789",
    referenceTaskIds = listOf("ref-task-1", "ref-task-2")
)

// Convert between JSON-RPC request models
val jsonRpcRequest: JsonRpcRequest = SendTaskRequest(
    jsonrpc = "2.0",
    id = StringValue("req-123"),
    params = TaskSendParams(
        id = "task-123",
        message = message
    )
)
val jsonrpcRequest = jsonRpcRequest.toJSONRPCRequest()
val convertedJsonRpcRequest = jsonrpcRequest.toJsonRpcRequest()
```

## Compatibility Considerations

- All enhanced models maintain backward compatibility through default values for new fields.
- Conversion utilities help bridge between enhanced and simple models.
- Multiple metadata options allow for different levels of type safety and flexibility.
- The Part type discriminator has changed from "type" to "kind" to match the Java implementation.

## Design Decisions

- **Type Safety**: Maintained Kotlin's type safety while adding compatibility with the Java implementation.
- **Backward Compatibility**: Ensured all changes maintain backward compatibility with existing code.
- **Flexibility**: Provided multiple options for metadata handling to balance type safety and flexibility.
- **Documentation**: Added KDoc comments to all new fields and classes to explain their purpose and usage.