# ── Stage 1: build ─────────────────────────────
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
RUN chmod +x gradlew

COPY build.gradle settings.gradle ./

RUN ./gradlew dependencies --no-daemon

COPY src src

RUN ./gradlew bootJar --no-daemon -x test


# ── Stage 2: run ──────────────────────────────
FROM eclipse-temurin:17-jre

WORKDIR /app

RUN groupadd appgroup && useradd -g appgroup appuser

COPY --from=builder /app/build/libs/app.jar app.jar

RUN mkdir -p /data /export \
 && chown -R appuser:appgroup /data /export

USER appuser

EXPOSE 8080

ENTRYPOINT ["java","-Xms256m","-Xmx512m","-jar","app.jar"]
