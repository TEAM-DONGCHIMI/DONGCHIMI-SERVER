plugins {
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":core"))
    implementation("org.springframework.boot:spring-boot-starter")

    implementation(platform("software.amazon.awssdk:bom:2.46.21"))
    implementation("software.amazon.awssdk:s3")
    implementation("software.amazon.awssdk:s3-presigner")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
