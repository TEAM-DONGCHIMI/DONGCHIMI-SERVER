plugins {
    kotlin("plugin.spring")
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":api:core-api"))
    implementation(project(":api:owner-api"))
    implementation(project(":api:admin-api"))
    implementation(project(":api:user-api"))
    implementation(project(":gateway:auth"))
    implementation(project(":gateway:logging"))
    implementation(project(":infrastructure:db"))
    implementation(project(":infrastructure:client"))
    implementation(project(":infrastructure:storage"))
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("net.logstash.logback:logstash-logback-encoder:8.0")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
