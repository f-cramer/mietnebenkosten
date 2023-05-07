package de.cramer.nebenkosten.jpa.security

import de.cramer.nebenkosten.entities.Contract
import de.cramer.nebenkosten.entities.Contract_
import de.cramer.nebenkosten.entities.Flat_
import de.cramer.nebenkosten.entities.RentalComplex
import de.cramer.nebenkosten.entities.Tenant_
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.stereotype.Component

@Component
class ContractRentalComplexApplier : RentalComplexApplier<Contract> {
    override fun check(entity: Contract, rentalComplex: RentalComplex): Boolean {
        return entity.flat.rentalComplex == rentalComplex &&
            entity.tenant.rentalComplex == rentalComplex
    }

    override fun getPredicate(rentalComplex: RentalComplex, root: Root<Contract>, criteriaBuilder: CriteriaBuilder): Predicate {
        return criteriaBuilder.and(
            criteriaBuilder.equal(root.join(Contract_.flat).get(Flat_.rentalComplex), rentalComplex),
            criteriaBuilder.equal(root.join(Contract_.tenant).get(Tenant_.rentalComplex), rentalComplex),
        )
    }
}
