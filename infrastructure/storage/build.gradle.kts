plugins {
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":core"))
    implementation("org.springframework.boot:spring-boot-starter")

    implementation("software.amazon.awssdk:s3:2.46.21")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
