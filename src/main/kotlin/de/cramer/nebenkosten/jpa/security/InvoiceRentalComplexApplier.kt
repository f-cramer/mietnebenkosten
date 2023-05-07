package de.cramer.nebenkosten.jpa.security

import de.cramer.nebenkosten.entities.Invoice
import de.cramer.nebenkosten.entities.Invoice_
import de.cramer.nebenkosten.entities.RentalComplex
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.stereotype.Component

@Component
class InvoiceRentalComplexApplier : RentalComplexApplier<Invoice> {
    override fun check(entity: Invoice, rentalComplex: RentalComplex): Boolean {
        return entity.rentalComplex == rentalComplex
    }

    override fun getPredicate(rentalComplex: RentalComplex, root: Root<Invoice>, criteriaBuilder: CriteriaBuilder): Predicate {
        return criteriaBuilder.equal(root.get(Invoice_.rentalComplex), rentalComplex)
    }
}
