plugins {
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":core"))
    implementation("org.springframework.boot:spring-boot-starter")

    // BOM은 이 프로젝트에서 하위 소비 모듈(bootstrap)로 버전이 전파되지 않아, 다른 서드파티 라이브러리와 동일하게 버전을 직접 명시한다.
    implementation("com.google.zxing:core:3.5.4") // QRCodeWriter (BitMatrix 생성)
    implementation("com.google.zxing:javase:3.5.4") // MatrixToImageWriter (PNG 인코딩)
}
