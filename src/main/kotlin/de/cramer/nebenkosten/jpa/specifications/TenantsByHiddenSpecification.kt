package de.cramer.nebenkosten.jpa.specifications

import de.cramer.nebenkosten.entities.Tenant
import de.cramer.nebenkosten.entities.Tenant_
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification
import java.io.Serial

data class TenantsByHiddenSpecification(
    private val hidden: Boolean,
) : Specification<Tenant> {
    override fun toPredicate(root: Root<Tenant>, query: CriteriaQuery<*>, criteriaBuilder: CriteriaBuilder): Predicate? {
        return criteriaBuilder.equal(root.get(Tenant_.hidden), hidden)
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 322401583598246382L
    }
}
