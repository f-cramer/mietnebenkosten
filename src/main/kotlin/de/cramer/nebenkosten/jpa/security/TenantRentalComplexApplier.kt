package de.cramer.nebenkosten.jpa.security

import de.cramer.nebenkosten.entities.RentalComplex
import de.cramer.nebenkosten.entities.Tenant
import de.cramer.nebenkosten.entities.Tenant_
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.stereotype.Component

@Component
class TenantRentalComplexApplier : RentalComplexApplier<Tenant> {
    override fun check(entity: Tenant, rentalComplex: RentalComplex): Boolean =
        entity.rentalComplex == rentalComplex

    override fun getPredicate(rentalComplex: RentalComplex, root: Root<Tenant>, criteriaBuilder: CriteriaBuilder): Predicate =
        criteriaBuilder.equal(root.get(Tenant_.rentalComplex), rentalComplex)
}
