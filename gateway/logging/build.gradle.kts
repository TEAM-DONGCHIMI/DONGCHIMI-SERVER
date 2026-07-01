plugins {
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":core"))
    compileOnly("jakarta.servlet:jakarta.servlet-api")
    implementation("org.springframework:spring-web")
    implementation("org.springframework:spring-context")
    implementation("org.slf4j:slf4j-api")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
}