# syntax=docker/dockerfile:1

# CI 러너에서 이미 './gradlew build -x test'로 bootJar를 빌드해두므로
# 이미지 안에서 Gradle을 다시 실행하지 않고 산출물만 담는다.
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# actuator healthcheck용 curl (jre 이미지에 기본 미포함)
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY bootstrap/build/libs/*.jar app.jar

# 힙 덤프(OOM)/GC 로그 출력 디렉터리 (compose에서 볼륨 마운트해 영속)
RUN mkdir -p /app/dumps /app/logs

EXPOSE 8080

# 메모리/GC 튜닝값(JAVA_OPTS)은 compose에서 주입 → 이미지 리빌드 없이 조정 가능
# exec로 java를 PID 1로 승격 → SIGTERM이 전달되어 Spring graceful shutdown 동작
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
