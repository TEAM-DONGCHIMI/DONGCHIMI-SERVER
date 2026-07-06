plugins {
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":common"))
    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-tx")
}
