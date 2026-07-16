plugins {
    kotlin("plugin.spring")
    id("org.springframework.boot")
}

// CI에서 build/libs/*.jar를 그대로 Docker 이미지에 COPY하므로 plain jar는 만들지 않는다.
tasks.named("jar") {
    enabled = false
}

dependencies {
    implementation(project(":core"))
    implementation(project(":api:core-api"))
    implementation(project(":api:owner-api"))
    implementation(project(":api:admin-api"))
    implementation(project(":api:user-api"))
    implementation(project(":gateway:auth"))
    implementation(project(":gateway:logging"))
    implementation(project(":infrastructure:db"))
    implementation(project(":infrastructure:client"))
    implementation(project(":infrastructure:storage"))
    implementation(project(":infrastructure:redis"))
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("net.logstash.logback:logstash-logback-encoder:8.0")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.21.4"))
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:testcontainers")
}

// spring-boot-dependencies가 org.testcontainers:testcontainers만 2.x로 강제하는데,
// org.testcontainers:postgresql은 아직 2.x가 없어 서로 다른 major 버전이 섞인다. 전부 1.21.4로 고정한다.
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.testcontainers") {
            useVersion("1.21.4")
        }
    }
}
