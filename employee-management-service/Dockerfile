# ============================================================
# Multi-stage Dockerfile for Employee Management Service
# ============================================================

# ---- Stage 1: Build ----
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /build

# Copy Maven wrapper and POM first (layer-cache friendly)
COPY mvnw ./
COPY .mvn .mvn
COPY pom.xml ./

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -q

# Copy source and build the fat JAR (skip tests — run them in CI)
COPY src ./src
RUN ./mvnw package -DskipTests -q

# Extract the layered JAR for optimised layer caching
RUN java -Djarmode=layertools -jar target/*.jar extract --destination /build/layers

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:17-jre-alpine AS runtime

# Security: run as non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy layered dependencies (most stable layers first)
COPY --from=builder /build/layers/dependencies          ./
COPY --from=builder /build/layers/spring-boot-loader    ./
COPY --from=builder /build/layers/snapshot-dependencies ./
COPY --from=builder /build/layers/application           ./

# Create log directory with correct permissions
RUN mkdir -p /app/logs && chown -R appuser:appgroup /app/logs

USER appuser

EXPOSE 8080

# Health check — Docker will mark the container unhealthy if this fails
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "org.springframework.boot.loader.launch.JarLauncher"]
