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

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]