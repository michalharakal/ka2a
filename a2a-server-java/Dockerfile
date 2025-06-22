FROM eclipse-temurin:17-jdk as builder
WORKDIR /app
COPY . .
RUN ./gradlew bootJar

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENV OPENAI_BASE_URL=https://api.openai.com/v1
ENV OPENAI_API_KEY=your-api-key
ENV OPENAI_CHAT_MODEL=gpt-3.5-turbo
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]
