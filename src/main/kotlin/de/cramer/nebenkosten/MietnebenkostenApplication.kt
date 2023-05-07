package de.cramer.nebenkosten

import de.cramer.nebenkosten.jpa.RentalComplexAwareJpaRepositoryFactoryBean
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.repository.query.QueryLookupStrategy

@SpringBootApplication
@EnableJpaRepositories(repositoryFactoryBeanClass = RentalComplexAwareJpaRepositoryFactoryBean::class, queryLookupStrategy = QueryLookupStrategy.Key.USE_DECLARED_QUERY)
class MietnebenkostenApplication

fun main(args: Array<String>) {
    runApplication<MietnebenkostenApplication>(*args)
}
