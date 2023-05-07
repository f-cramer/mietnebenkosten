package de.cramer.nebenkosten.jpa.security

import de.cramer.nebenkosten.entities.Flat
import de.cramer.nebenkosten.entities.Flat_
import de.cramer.nebenkosten.entities.RentalComplex
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.stereotype.Component

@Component
class FlatRentalComplexApplier : RentalComplexApplier<Flat> {
    override fun check(entity: Flat, rentalComplex: RentalComplex): Boolean {
        return entity.rentalComplex == rentalComplex
    }

    override fun getPredicate(rentalComplex: RentalComplex, root: Root<Flat>, criteriaBuilder: CriteriaBuilder): Predicate {
        return criteriaBuilder.equal(root.get(Flat_.rentalComplex), rentalComplex)
    }
}
