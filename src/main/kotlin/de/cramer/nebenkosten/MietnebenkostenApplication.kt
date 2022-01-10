package de.cramer.nebenkosten

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InjectionPoint
import org.springframework.beans.factory.config.ConfigurableBeanFactory.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope

@SpringBootApplication
class MietnebenkostenApplication {

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    fun logger(injectionPoint: InjectionPoint): Logger {
        val type = injectionPoint.member.declaringClass
        return LoggerFactory.getLogger(type)
    }
}

fun main(args: Array<String>) {
    runApplication<MietnebenkostenApplication>(*args)
}
