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
    // ImportJobPoller의 @PostConstruct/@PreDestroy
    implementation("jakarta.annotation:jakarta.annotation-api")
}
