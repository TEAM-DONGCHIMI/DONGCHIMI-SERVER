plugins {
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":core"))
    compileOnly("jakarta.servlet:jakarta.servlet-api")
    compileOnly("org.springframework.boot:spring-boot-starter-webmvc") {
        exclude(group = "org.springframework.boot", module = "spring-boot-validation")
    }
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-security-oauth2-client")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
    testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-client-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
}
