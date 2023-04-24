import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    kotlin("plugin.allopen")
    kotlin("kapt")

    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
}

group = "de.cramer.nebenkosten"
version = properties["VERSION"] as String
java.sourceCompatibility = JavaVersion.toVersion(properties["JAVA_VERSION"] as String)

ext["kotlin.version"] = properties["KOTLIN_VERSION"] as String

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

    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")

    val jasperReportsVersion = properties["JASPERREPORTS_VERSION"] as String
    implementation("net.sf.jasperreports:jasperreports:$jasperReportsVersion")
    implementation("net.sf.jasperreports:jasperreports-functions:$jasperReportsVersion")

    // cve mitigation
    implementation("org.apache.commons:commons-collections4:4.4")

    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    kapt("org.springframework.boot:spring-boot-configuration-processor")
    kapt("org.hibernate.orm:hibernate-jpamodelgen")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = properties["JAVA_VERSION"] as String
        allWarningsAsErrors = true
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "7.6.1"
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
