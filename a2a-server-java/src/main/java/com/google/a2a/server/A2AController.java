package com.google.a2a.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.a2a.model.A2AError;
import com.google.a2a.model.AgentCard;
import com.google.a2a.model.ErrorCode;
import com.google.a2a.model.JSONRPCError;
import com.google.a2a.model.JSONRPCRequest;
import com.google.a2a.model.JSONRPCResponse;
import com.google.a2a.model.SendTaskStreamingResponse;
import com.google.a2a.model.Task;
import com.google.a2a.model.TaskSendParams;
import com.google.a2a.model.TaskState;
import com.google.a2a.model.TaskStatus;
import com.google.a2a.model.TaskStatusUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

/**
 * A2A REST controller for handling JSON-RPC requests
 */
@RestController
public class A2AController {

    private static final Logger logger = LoggerFactory.getLogger(A2AController.class);

    private final A2AServer server;
    private final ObjectMapper objectMapper;

    public A2AController(A2AServer server, ObjectMapper objectMapper) {
        this.server = server;
        this.objectMapper = objectMapper;
    }

    /**
     * Handle JSON-RPC requests
     */
    @PostMapping(
            path = "/a2a",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<JSONRPCResponse> handleJsonRpcRequest(@RequestBody JSONRPCRequest request) {
        logger.info("Received JSON-RPC request: method={}, id={}", request.method(), request.id());

        if (!"2.0".equals(request.jsonrpc())) {
            String errorMessage = "Invalid JSON-RPC version";
            logger.error("Request error: {}, id={}", errorMessage, request.id());

            JSONRPCError error = new JSONRPCError(
                    ErrorCode.INVALID_REQUEST.getValue(),
                    errorMessage,
                    null
            );
            JSONRPCResponse response = new JSONRPCResponse(
                    request.id(),
                    "2.0",
                    null,
                    error
            );
            return ResponseEntity.badRequest().body(response);
        }

        JSONRPCResponse response = switch (request.method()) {
            case "tasks/send" -> server.handleTaskSend(request);
            case "tasks/get" -> server.handleTaskGet(request);
            case "tasks/cancel" -> server.handleTaskCancel(request);
            default -> {
                String errorMessage = "Method not found: " + request.method();
                logger.error("Request error: {}, id={}", errorMessage, request.id());

                JSONRPCError error = new JSONRPCError(
                        ErrorCode.METHOD_NOT_FOUND.getValue(),
                        "Method not found",
                        null
                );
                yield new JSONRPCResponse(
                        request.id(),
                        "2.0",
                        null,
                        error
                );
            }
        };

        // Log response status (success or error)
        if (response.error() != null) {
            logger.error("Response error: method={}, id={}, error={}", 
                request.method(), request.id(), response.error().message());
        } else {
            logger.info("Response success: method={}, id={}", request.method(), request.id());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Handle streaming task requests (Server-Sent Events)
     */
    @PostMapping(
            value = "/a2a/stream",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter handleStreamingTask(@RequestBody JSONRPCRequest request) {
        logger.info("Received streaming request: method={}, id={}", request.method(), request.id());

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        // Process task asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                if (!"tasks/send".equals(request.method())) {
                    String errorMessage = "Method not found for streaming: " + request.method();
                    logger.error("Streaming error: {}, id={}", errorMessage, request.id());
                    sendErrorEvent(emitter, request.id(), ErrorCode.METHOD_NOT_FOUND, "Method not found");
                    return;
                }

                TaskSendParams params = parseTaskSendParams(request.params());

                // Create initial status with timestamp
                TaskStatus initialStatus = new TaskStatus(
                    TaskState.WORKING,
                    null,  // No message initially
                    Instant.now().toString()
                );

                // Send initial status update
                TaskStatusUpdateEvent initialEvent = new TaskStatusUpdateEvent(
                        params.id(),
                        initialStatus,
                        false,  // final
                        null    // metadata
                );

                SendTaskStreamingResponse initialResponse = new SendTaskStreamingResponse(
                        request.id(),
                        "2.0",
                        initialEvent,
                        null
                );

                emitter.send(SseEmitter.event()
                        .name("task-update")
                        .data(objectMapper.writeValueAsString(initialResponse)));

                // Process task
                JSONRPCResponse taskResponse = server.handleTaskSend(request);

                if (taskResponse.error() != null) {
                    logger.error("Task processing error: method={}, id={}, error={}", 
                        request.method(), request.id(), taskResponse.error().message());
                    sendErrorEvent(emitter, request.id(), ErrorCode.INTERNAL_ERROR, taskResponse.error().message());
                    return;
                }

                // Send final status update
                Task completedTask = (Task) taskResponse.result();
                TaskStatusUpdateEvent finalEvent = new TaskStatusUpdateEvent(
                        completedTask.id(),
                        completedTask.status(),
                        true,   // final
                        null    // metadata
                );

                SendTaskStreamingResponse finalResponse = new SendTaskStreamingResponse(
                        request.id(),
                        "2.0",
                        finalEvent,
                        null
                );

                emitter.send(SseEmitter.event()
                        .name("task-update")
                        .data(objectMapper.writeValueAsString(finalResponse)));

                emitter.complete();

            } catch (Exception e) {
                logger.error("Exception during streaming task processing: method={}, id={}, error={}", 
                    request.method(), request.id(), e.getMessage(), e);
                sendErrorEvent(emitter, request.id(), ErrorCode.INTERNAL_ERROR, e.getMessage());
            }
        });

        return emitter;
    }

    /**
     * Get agent card information
     */
    @GetMapping("/.well-known/agent-card")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<AgentCard> getAgentCard() {
        logger.info("Received request for agent card");
        AgentCard agentCard = server.getAgentCard();
        logger.info("Returning agent card: name={}", agentCard.name());
        return ResponseEntity.ok(agentCard);
    }

    /**
     * Parse TaskSendParams
     */
    private TaskSendParams parseTaskSendParams(Object params) throws Exception {
        return objectMapper.convertValue(params, TaskSendParams.class);
    }

    /**
     * Send error event
     */
    private void sendErrorEvent(SseEmitter emitter, Object requestId, ErrorCode code, String message) {
        try {
            logger.error("Sending error event: id={}, code={}, message={}", requestId, code.getValue(), message);

            A2AError error = new A2AError(code, message, null);
            SendTaskStreamingResponse errorResponse = new SendTaskStreamingResponse(
                    requestId,
                    "2.0",
                    null,
                    error
            );

            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(objectMapper.writeValueAsString(errorResponse)));

            emitter.completeWithError(new RuntimeException(message));

        } catch (IOException e) {
            logger.error("Failed to send error event: id={}, error={}", requestId, e.getMessage(), e);
            emitter.completeWithError(e);
        }
    }
}
