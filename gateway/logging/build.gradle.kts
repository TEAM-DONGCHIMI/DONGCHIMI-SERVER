plugins {
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":core"))
    compileOnly("jakarta.servlet:jakarta.servlet-api")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
}