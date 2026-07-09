plugins {
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":core"))
    implementation("org.springframework:spring-context")
    implementation("org.apache.poi:poi-ooxml:5.5.1")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
