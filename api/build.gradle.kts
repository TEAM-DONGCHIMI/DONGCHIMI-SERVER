plugins {
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":core"))
    runtimeOnly(project(":gateway-auth"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
