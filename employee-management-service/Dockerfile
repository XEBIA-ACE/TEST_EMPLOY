# =============================================================================
# Multi-stage Dockerfile for Employee Management Service
# =============================================================================
# Stage 1 – Build
# =============================================================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy dependency manifests first to take advantage of layer caching
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

RUN chmod +x mvnw
# Download dependencies in a separate layer — only re-runs when pom.xml changes
RUN ./mvnw dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN ./mvnw package -DskipTests -B

# =============================================================================
# Stage 2 – Runtime (minimal JRE image)
# =============================================================================
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Create a non-root user for the process
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy only the fat JAR from the build stage
COPY --from=builder /app/target/*.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser

# Expose the default application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
