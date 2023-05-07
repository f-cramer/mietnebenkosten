package de.cramer.nebenkosten.jpa

import de.cramer.nebenkosten.config.rentalcomplex.RentalComplexResolver
import de.cramer.nebenkosten.jpa.security.RentalComplexApplier
import jakarta.persistence.EntityManager
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean
import org.springframework.data.repository.core.support.RepositoryFactorySupport

class RentalComplexAwareJpaRepositoryFactoryBean<T : Any, ID : Any>(
    repositoryInterface: Class<out RentalComplexAwareJpaRepository<T, ID>>,
    private val rentalComplexResolver: RentalComplexResolver,
    private val rentalComplexAppliers: List<RentalComplexApplier<*>>,
) : JpaRepositoryFactoryBean<RentalComplexAwareJpaRepository<T, ID>, T, ID>(repositoryInterface) {

    override fun createRepositoryFactory(entityManager: EntityManager): RepositoryFactorySupport =
        RentalComplexAwareJpaRepositoryFactory(entityManager, rentalComplexResolver, rentalComplexAppliers)
}
