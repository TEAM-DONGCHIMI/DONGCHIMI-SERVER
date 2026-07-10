plugins {
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":common"))
    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-tx")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    // MDC를 워커 코루틴으로 전파(ErrorNotificationDispatcher와 동일한 이유)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j")
    // ImportJobProperties(@ConfigurationProperties)를 위한 최소 의존성. 워커(Poller/Processor)가
    // core에 있어 설정도 여기서 바로 바인딩한다 — spring-boot-starter류가 아닌 순수 바인딩 API만 가져온다.
    implementation("org.springframework.boot:spring-boot")
    // ImportJobPoller의 @PostConstruct/@PreDestroy
    implementation("jakarta.annotation:jakarta.annotation-api")
}
