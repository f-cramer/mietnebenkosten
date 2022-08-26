import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    id("org.springframework.boot") version "2.7.3"
    id("io.spring.dependency-management") version "1.0.13.RELEASE"
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.spring") version "1.7.10"
    kotlin("plugin.jpa") version "1.7.10"
    kotlin("plugin.allopen") version "1.7.10"
    kotlin("kapt") version "1.7.10"

    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
}

group = "de.cramer.nebenkosten"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

allOpen {
    annotations("javax.persistence.Entity", "javax.persistence.MappedSuperclass")
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    maven(url = "http://jaspersoft.jfrog.io/artifactory/third-party-ce-artifacts/") {
        isAllowInsecureProtocol = true
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.flywaydb:flyway-core")

    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity5")

    val jasperReportsVersion = "6.20.0"
    implementation("net.sf.jasperreports:jasperreports:$jasperReportsVersion")
    implementation("net.sf.jasperreports:jasperreports-functions:$jasperReportsVersion")

    // cve mitigation
    implementation("org.apache.commons:commons-collections4:4.4")

    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    kapt("org.springframework.boot:spring-boot-configuration-processor")
    kapt("org.hibernate:hibernate-jpamodelgen")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
        allWarningsAsErrors = true
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "7.5.1"
}

ktlint {
    disabledRules.set(setOf("indent", "experimental:annotation", "experimental:trailing-comma"))
    enableExperimentalRules.set(true)

    reporters {
        reporter(ReporterType.PLAIN)
        reporter(ReporterType.HTML)
        reporter(ReporterType.CHECKSTYLE)
    }
}
