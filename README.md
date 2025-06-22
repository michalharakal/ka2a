# ka2a
PoC of KMP  based A2A client

https://google-a2a.github.io/A2A/latest/specification/

## Running the A2A Server

The A2A (Agent-to-Agent) server is bases of google's official A2A sample and is implemented in Java with Spring Boot and provides a translation service. See details to official sample https://a2aprotocol.ai/docs/guide/a2a-java-sample

Here's how to run it:

### Running Locally

1. Set the required environment variables:
   ```bash
   export OPENAI_BASE_URL=https://api.openai.com/v1
   export OPENAI_API_KEY=your-api-key
   export OPENAI_CHAT_MODEL=gpt-3.5-turbo
   ```

2. Navigate to the server-java directory:
   ```bash
   cd server-java
   ```

3. Build and run the application:
   ```bash
   ./gradlew bootRun
   ```

The server will start on port 8090. You can access the agent card at http://localhost:8090/.well-known/agent-card

### Running with Docker

1. Navigate to the server-java directory:
   ```bash
   cd server-java
   ```

2. Build the Docker image:
   ```bash
   docker build -t a2a-server .
   ```

3. Run the Docker container:
   ```bash
   docker run -p 8090:8090 \
     -e OPENAI_BASE_URL=https://api.openai.com/v1 \
     -e OPENAI_API_KEY=your-api-key \
     -e OPENAI_CHAT_MODEL=gpt-3.5-turbo \
     a2a-server
   ```

The server will be accessible at http://localhost:8090.

