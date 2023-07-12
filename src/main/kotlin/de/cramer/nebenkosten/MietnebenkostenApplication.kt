package de.cramer.nebenkosten

import de.cramer.nebenkosten.aot.JasperreportsRuntimeHints
import de.cramer.nebenkosten.aot.ThymeleafRuntimeHints
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ImportRuntimeHints
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.repository.query.QueryLookupStrategy

@ImportRuntimeHints(JasperreportsRuntimeHints::class, ThymeleafRuntimeHints::class)
@SpringBootApplication
@EnableJpaRepositories(queryLookupStrategy = QueryLookupStrategy.Key.USE_DECLARED_QUERY)
class MietnebenkostenApplication

fun main(args: Array<String>) {
    runApplication<MietnebenkostenApplication>(*args)
}
