import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.spring") version "2.1.20"
    kotlin("plugin.jpa") version "2.2.20"
    kotlin("plugin.allopen") version "2.1.20"
    kotlin("kapt") version "2.1.20"

    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("com.github.ben-manes.versions") version "0.53.0"
    id("io.github.f-cramer.jasperreports") version "0.0.4"
}

group = "de.cramer.nebenkosten"
version = properties["VERSION"] as String
java.sourceCompatibility = JavaVersion.toVersion(properties["JAVA_VERSION"] as String)

ext["kotlin.version"] = kotlin.coreLibrariesVersion

allOpen {
    annotations("jakarta.persistence.Entity", "jakarta.persistence.MappedSuperclass")
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
    runtimeOnly("org.flywaydb:flyway-database-postgresql")

    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")

    val jasperReportsVersion = "6.21.3"
    implementation("net.sf.jasperreports:jasperreports:$jasperReportsVersion") {
        exclude(group = "commons-logging")
    }
    implementation("net.sf.jasperreports:jasperreports-functions:$jasperReportsVersion") {
        exclude(group = "commons-logging")
    }

    // cve mitigation
    implementation("org.apache.commons:commons-collections4:4.5.0")

    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    kapt("org.springframework.boot:spring-boot-configuration-processor")
    kapt("org.hibernate.orm:hibernate-jpamodelgen")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.set(listOf("-Xjsr305=strict", "-Xsuppress-version-warnings"))
        jvmTarget.set(JvmTarget.valueOf("JVM_${properties["JAVA_VERSION"]}"))
        allWarningsAsErrors.set(true)
    }
}

tasks.withType<Jar>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<Wrapper> {
    gradleVersion = project.extra["WRAPPER_VERSION"] as String
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    gradleReleaseChannel = "current"

    val ignoredVersions = listOf("rc", "beta", "-m")
    val managedVersions = dependencyManagement.managedVersions.keys.toSet() +
        setOf("io.github.oshai:kotlin-logging")

    rejectVersionIf {
        if (ignoredVersions.any { candidate.version.lowercase().contains(it) }) {
            return@rejectVersionIf true
        }

        val dependency = "${candidate.group}:${candidate.module}"
        dependency in managedVersions
    }
}

val checkGradleWrapperVersion: TaskProvider<Task> = tasks.register("checkGradleWrapperVersion") {
    val propertiesVersion = GradleVersion.version(project.extra["WRAPPER_VERSION"] as String)
    doFirst {
        val currentVersion = GradleVersion.current()
        if (currentVersion != propertiesVersion) {
            throw GradleException("current gradle wrapper version (${currentVersion.version}) does not match property WRAPPER_VERSION (${propertiesVersion.version})")
        }
    }
}
tasks.named("check").configure {
    dependsOn(checkGradleWrapperVersion)
}

dependencyManagement {
    configurations.getByName("detekt") {
        dependencies {
            dependencySet("org.jetbrains.kotlin:2.0.21") {
                entry("kotlin-compiler-embeddable")
            }
        }
    }
}

ktlint {
    version.set(project.extra["KTLINT_VERSION"] as String)
    enableExperimentalRules.set(true)
    additionalEditorconfig.putAll(
        mapOf(
            "ktlint_code_style" to "intellij_idea",
        ),
    )
    reporters {
        reporter(ReporterType.PLAIN)
        reporter(ReporterType.HTML)
        reporter(ReporterType.CHECKSTYLE)
    }
}

detekt {
    buildUponDefaultConfig = true
    config.from(".config/detekt.yml")
}

jasperreports {
    classpath.from(configurations.compileClasspath)
}

tasks.processResources {
    dependsOn(tasks.compileAllReports)
}

sourceSets.main {
    resources.srcDir(jasperreports.outDir)
}
