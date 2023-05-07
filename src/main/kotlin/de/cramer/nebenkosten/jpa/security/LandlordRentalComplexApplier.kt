package de.cramer.nebenkosten.jpa.security

import de.cramer.nebenkosten.entities.Landlord
import de.cramer.nebenkosten.entities.Landlord_
import de.cramer.nebenkosten.entities.RentalComplex
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.stereotype.Component

@Component
class LandlordRentalComplexApplier : RentalComplexApplier<Landlord> {
    override fun check(entity: Landlord, rentalComplex: RentalComplex): Boolean {
        return entity.rentalComplex == rentalComplex
    }

    override fun getPredicate(rentalComplex: RentalComplex, root: Root<Landlord>, criteriaBuilder: CriteriaBuilder): Predicate {
        return criteriaBuilder.equal(root.get(Landlord_.rentalComplex), rentalComplex)
    }
}
