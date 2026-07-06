plugins {
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":core"))
    implementation("org.springframework.boot:spring-boot-starter")

    implementation("software.amazon.awssdk:s3:2.46.21")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testImplementation(platform("org.testcontainers:testcontainers-bom:1.21.4"))
    testImplementation("org.testcontainers:localstack")
}

// spring-boot-dependencies가 org.testcontainers:testcontainers만 2.x로 강제하는데,
// org.testcontainers:localstack은 아직 2.x가 없어 서로 다른 major 버전이 섞인다. 전부 1.21.4로 고정한다.
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.testcontainers") {
            useVersion("1.21.4")
        }
    }
}
