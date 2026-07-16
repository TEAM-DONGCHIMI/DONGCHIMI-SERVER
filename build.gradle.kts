plugins {
    kotlin("jvm") version "2.3.21" apply false
    kotlin("plugin.spring") version "2.3.21" apply false
    id("org.springframework.boot") version "4.1.0" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    kotlin("plugin.jpa") version "2.3.21" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2" apply false
}

tasks.register<Exec>("installGitHooks") {
    // git 저장소가 아닌 환경(Docker 이미지 빌드 등)에서는 스킵한다.
    // configuration cache 호환을 위해 Project 참조 대신 구성 시점에 평가한 값을 캡처한다.
    val isGitRepo = rootProject.file(".git").exists()
    onlyIf { isGitRepo }
    commandLine("git", "config", "core.hooksPath", ".githooks")
}

allprojects {
    group = "kr.dongchimi"
    version = "0.0.1"
    description = "dongchimi-server"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.5.0")
    }

    configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:4.1.0")
        }
    }

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    tasks.matching { it.name == "compileKotlin" }.configureEach {
        dependsOn(rootProject.tasks["installGitHooks"])
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
        }
    }

    dependencies {
        "implementation"("org.jetbrains.kotlin:kotlin-reflect")
        "testImplementation"("org.jetbrains.kotlin:kotlin-test-junit5")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")

        "testImplementation"(platform("io.kotest:kotest-bom:6.2.1"))
        "testImplementation"("io.kotest:kotest-runner-junit5")
        "testImplementation"("io.kotest:kotest-assertions-core")

        "implementation"("io.github.oshai:kotlin-logging-jvm:7.0.3")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
