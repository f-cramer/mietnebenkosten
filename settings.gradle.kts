rootProject.name = "nebenkosten"

pluginManagement {
    plugins {
        val kotlinVersion = extra["KOTLIN_VERSION"] as String
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.spring") version kotlinVersion
        kotlin("plugin.jpa") version kotlinVersion
        kotlin("plugin.allopen") version kotlinVersion
        kotlin("kapt") version kotlinVersion

        val springBootVersion = extra["SPRING_BOOT_VERSION"] as String
        id("org.springframework.boot") version springBootVersion

        val springDependencyManagementVersion = extra["SPRING_DEPENDENCY_MANAGEMENT_VERSION"] as String
        id("io.spring.dependency-management") version springDependencyManagementVersion
    }
}
