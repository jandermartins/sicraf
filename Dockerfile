# =============================================================================
# SICRAF - Dockerfile Multi-Stage para Fly.io
# =============================================================================

# ================================
# Stage 1: Build com Gradle
# ================================
FROM gradle:8-jdk21 AS builder

WORKDIR /app

# Copia arquivos de build primeiro (melhor cache de camadas)
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Baixa dependências (camada cacheável)
RUN gradle dependencies --no-daemon || true

# Copia código fonte
COPY src ./src

# Build do jar (sem testes para acelerar)
RUN gradle bootJar --no-daemon -x test

# ================================
# Stage 2: Runtime minimalista
# ================================
FROM eclipse-temurin:21-jre AS runtime

WORKDIR /app

# Cria usuário não-root (segurança)
RUN addgroup --system appuser && \
    adduser --system --ingroup appuser appuser

# Copia o jar do builder
COPY --from=builder /app/build/libs/*.jar app.jar

# Ajusta ownership
RUN chown -R appuser:appuser /app

USER appuser

# JVM container-aware (respeita limites de memória do container)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

# Porta padrão do Fly.io
EXPOSE 8080

# Health check (Fly.io usa isso para saber se o app está pronto)
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Comando de inicialização
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]