plugins {
    id("dongchimi.api-module-conventions")
}

dependencies {
    implementation(project(":common"))
    // SSE 구독 시 Flow<ImportJobEvent>를 SseEmitter로 브리지하는 코루틴에 필요
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j")
    // suspend 컨트롤러 메서드(cancelImport)를 Spring MVC가 실행할 때 CoroutinesUtils가 내부적으로
    // kotlinx.coroutines.reactor.MonoKt를 호출하므로 런타임에 필요
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
}
