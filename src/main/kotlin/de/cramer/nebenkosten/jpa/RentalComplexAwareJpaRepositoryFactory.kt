package de.cramer.nebenkosten.jpa

import de.cramer.nebenkosten.config.rentalcomplex.RentalComplexResolver
import de.cramer.nebenkosten.jpa.security.RentalComplexApplier
import jakarta.persistence.EntityManager
import org.springframework.core.ResolvableType
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.data.repository.core.RepositoryInformation
import org.springframework.data.repository.core.RepositoryMetadata

class RentalComplexAwareJpaRepositoryFactory(
    entityManager: EntityManager,
    private val rentalComplexResolver: RentalComplexResolver,
    rentalComplexAppliers: List<RentalComplexApplier<*>>,
) : JpaRepositoryFactory(entityManager) {

    private val rentalComplexAppliers: Map<Class<*>, RentalComplexApplier<*>>

    init {
        this.rentalComplexAppliers = rentalComplexAppliers.associateBy {
            ResolvableType.forInstance(it)
                .`as`(RentalComplexApplier::class.java)
                .resolveGeneric(0)!!
        }
    }

    override fun getTargetRepository(information: RepositoryInformation, entityManager: EntityManager): JpaRepositoryImplementation<*, *> {
        return getTargetRepository(information.domainType, entityManager)
    }

    private fun <T : Any> getTargetRepository(domainType: Class<T>, entityManager: EntityManager): JpaRepositoryImplementation<*, *> {
        val rentalComplexApplier = getRentalComplexApplier(domainType)
        val entityInformation = JpaEntityInformationSupport.getEntityInformation(domainType, entityManager)
        return if (rentalComplexApplier == null) {
            SimpleJpaRepository<T, Any>(entityInformation, entityManager)
        } else {
            RentalComplexAwareJpaRepository<T, Any>(entityInformation, entityManager, rentalComplexResolver, rentalComplexApplier)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getRentalComplexApplier(domainType: Class<T>): RentalComplexApplier<T>? =
        rentalComplexAppliers[domainType] as? RentalComplexApplier<T>

    override fun getRepositoryBaseClass(metadata: RepositoryMetadata): Class<*> =
        RentalComplexAwareJpaRepository::class.java
}
