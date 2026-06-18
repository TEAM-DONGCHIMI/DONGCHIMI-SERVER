plugins {
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":core"))
    runtimeOnly(project(":gateway-auth"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("tools.jackson.module:jackson-module-kotlin")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}