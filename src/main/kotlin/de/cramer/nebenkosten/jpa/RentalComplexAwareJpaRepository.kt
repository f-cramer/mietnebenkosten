package de.cramer.nebenkosten.jpa

import de.cramer.nebenkosten.config.rentalcomplex.RentalComplexResolver
import de.cramer.nebenkosten.entities.RentalComplex
import de.cramer.nebenkosten.jpa.security.RentalComplexApplier
import jakarta.persistence.EntityManager
import jakarta.persistence.EntityNotFoundException
import jakarta.persistence.TypedQuery
import jakarta.persistence.criteria.Root
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import java.util.Optional

class RentalComplexAwareJpaRepository<T : Any, ID : Any>(
    entityInformation: JpaEntityInformation<T, *>,
    private val entityManager: EntityManager,
    private val rentalComplexResolver: RentalComplexResolver,
    private val rentalComplexApplier: RentalComplexApplier<T>,
) : SimpleJpaRepository<T, ID>(entityInformation, entityManager) {
    override fun findById(id: ID): Optional<T> {
        return super.findById(id).filter(this::check)
    }

    override fun getReferenceById(id: ID): T {
        val reference = super.getReferenceById(id)
        if (!check(reference)) throw EntityNotFoundException()
        return reference
    }

    private fun check(entity: T): Boolean {
        val rentalComplex = rentalComplexResolver.getCurrentRentalComplex()
        return rentalComplex == null || rentalComplexApplier.check(entity, rentalComplex)
    }

    override fun <S : T> getQuery(spec: Specification<S>?, domainClass: Class<S>, sort: Sort): TypedQuery<S> {
        return super.getQuery(adjustSpecification(spec), domainClass, sort)
    }

    override fun <S : T> getCountQuery(spec: Specification<S>?, domainClass: Class<S>): TypedQuery<Long> {
        return super.getCountQuery(adjustSpecification(spec), domainClass)
    }

    private fun <S : T> adjustSpecification(spec: Specification<S>?): Specification<S>? {
        val rentalComplex = rentalComplexResolver.getCurrentRentalComplex()
        return if (rentalComplex == null) spec else getSpecification<S>(rentalComplex).and(spec)
    }

    private fun <S : T> getSpecification(rentalComplex: RentalComplex) = Specification<S> { root, _, criteriaBuilder ->
        @Suppress("UNCHECKED_CAST")
        val tRoot = root as Root<T>
        rentalComplexApplier.getPredicate(rentalComplex, tRoot, criteriaBuilder)
    }

    override fun delete(entity: T) {
        if (check(entity)) super.delete(entity)
    }

    override fun deleteAllByIdInBatch(ids: MutableIterable<ID>) {
        val entities = findAllById(ids)
        super.deleteAllInBatch(entities)
    }

    override fun deleteAllInBatch() {
        throw UnsupportedOperationException()
    }
}
