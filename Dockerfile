# syntax=docker/dockerfile:1

# --- build stage ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# 의존성 레이어 캐싱: 빌드 스크립트/래퍼를 먼저 복사
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
COPY buildSrc buildSrc
RUN chmod +x gradlew

# 전체 소스 복사 후 bootJar 빌드 (테스트는 CI 별도 스텝에서 실행)
COPY . .
RUN ./gradlew :bootstrap:bootJar -x test --no-daemon

# --- runtime stage ---
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# actuator healthcheck용 curl (jre 이미지에 기본 미포함)
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/bootstrap/build/libs/*.jar app.jar

# 힙 덤프(OOM)/GC 로그 출력 디렉터리 (compose에서 볼륨 마운트해 영속)
RUN mkdir -p /app/dumps /app/logs

EXPOSE 8080

# 메모리/GC 튜닝값(JAVA_OPTS)은 compose에서 주입 → 이미지 리빌드 없이 조정 가능
# exec로 java를 PID 1로 승격 → SIGTERM이 전달되어 Spring graceful shutdown 동작
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]