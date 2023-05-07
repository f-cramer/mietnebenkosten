package de.cramer.nebenkosten.jpa.security

import de.cramer.nebenkosten.entities.RentalComplex
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root

interface RentalComplexApplier<T> {
    fun check(entity: T, rentalComplex: RentalComplex): Boolean

    fun getPredicate(rentalComplex: RentalComplex, root: Root<T>, criteriaBuilder: CriteriaBuilder): Predicate
}
