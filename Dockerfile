# =============================================================================
# Multi-stage Dockerfile for the Employee Management Service
# =============================================================================
# Stage 1 — Build  (Maven + JDK 17)
# Stage 2 — Runtime (JRE 17-slim, distroless-style)
# =============================================================================

# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /workspace

# Copy dependency descriptors first to exploit layer caching.
# Maven only re-downloads dependencies when pom.xml changes.
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

RUN chmod +x mvnw && ./mvnw dependency:go-offline -B --no-transfer-progress

# Copy the full source
COPY src/ src/

# Build the fat JAR, skipping tests (tests run in CI, not here)
RUN ./mvnw package -DskipTests -B --no-transfer-progress

# Extract layers for efficient Docker caching (Spring Boot 2.3+)
RUN java -Djarmode=layertools \
    -jar target/employee-management-service-*.jar extract \
    --destination /workspace/extracted


# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime

# Security: run as a non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

WORKDIR /app

# Copy Spring Boot layers in dependency-change-frequency order
# (least volatile first → most volatile last) for optimal layer reuse
COPY --from=builder /workspace/extracted/dependencies/          ./
COPY --from=builder /workspace/extracted/spring-boot-loader/    ./
COPY --from=builder /workspace/extracted/snapshot-dependencies/ ./
COPY --from=builder /workspace/extracted/application/           ./

# Expose the default port
EXPOSE 8080

# JVM tuning for containers:
#   - UseContainerSupport: honour cgroup memory limits
#   - MaxRAMPercentage: use up to 75 % of the container's RAM for the heap
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]

# Health check via the actuator endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1
