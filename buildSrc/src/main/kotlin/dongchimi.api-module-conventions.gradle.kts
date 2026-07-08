apply(plugin = "org.jetbrains.kotlin.plugin.spring")

dependencies {
    "implementation"(project(":api:core-api"))
    "implementation"(project(":core"))
    "runtimeOnly"(project(":gateway:auth"))
    "implementation"(project(":gateway:logging"))
    "implementation"("org.springframework.boot:spring-boot-starter-webmvc") {
        exclude(group = "org.springframework.boot", module = "spring-boot-validation")
    }
    "implementation"("org.springframework:spring-tx")
    "implementation"("tools.jackson.module:jackson-module-kotlin")
    "implementation"("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")
    "testImplementation"("org.springframework.boot:spring-boot-starter-test")
}
