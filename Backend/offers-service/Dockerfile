# ═══════════════════════════════════════════════════════════════════════════
#  SMARTEK — offers-service  |  Dockerfile multi-stage
#  Stage 1 : Build Maven (cache des dépendances)
#  Stage 2 : Runtime JRE Alpine (image minimale)
# ═══════════════════════════════════════════════════════════════════════════

# ── Stage 1 : Build ────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS build

ARG BUILD_DATE
ARG GIT_COMMIT=unknown

WORKDIR /app

# Copier uniquement le pom.xml d'abord pour profiter du cache des dépendances
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B --no-transfer-progress

# Copier les sources et builder
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -DskipTests -B --no-transfer-progress \
    -Dmaven.wagon.http.retryHandler.count=3

# ── Stage 2 : Runtime ──────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

# Métadonnées OCI
LABEL org.opencontainers.image.title="offers-service" \
      org.opencontainers.image.description="SMARTEK Offers Management Microservice" \
      org.opencontainers.image.vendor="SMARTEK" \
      org.opencontainers.image.version="1.0.0" \
      org.opencontainers.image.created="${BUILD_DATE}" \
      org.opencontainers.image.revision="${GIT_COMMIT}"

# Utilisateur non-root pour la sécurité
RUN addgroup -S smartek && adduser -S smartek -G smartek

WORKDIR /app

# Copier le JAR depuis le stage build
COPY --from=build --chown=smartek:smartek /app/target/*.jar app.jar

# Répertoire de logs
RUN mkdir -p /app/logs && chown smartek:smartek /app/logs

USER smartek

EXPOSE 8085

# Options JVM optimisées pour les conteneurs
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/app/logs/heapdump.hprof \
               -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
