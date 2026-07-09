plugins {
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":core"))
    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-web")
    implementation("org.springframework.boot:spring-boot")
    implementation("org.springframework.boot:spring-boot-starter-restclient")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("io.sentry:sentry:8.47.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    // QR 생성. BOM은 이 프로젝트에서 하위 소비 모듈(bootstrap)로 버전이 전파되지 않아, 다른 서드파티 라이브러리와 동일하게 버전을 직접 명시한다.
    implementation("com.google.zxing:core:3.5.4") // QRCodeWriter (BitMatrix 생성)
    implementation("com.google.zxing:javase:3.5.4") // MatrixToImageWriter (PNG 인코딩)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
