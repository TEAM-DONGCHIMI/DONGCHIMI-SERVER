plugins {
    id("dongchimi.api-module-conventions")
}

dependencies {
    implementation(project(":common"))
    // SSE 구독 시 Flow<ImportJobEvent>를 SseEmitter로 브리지하는 코루틴에 필요
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j")
}
