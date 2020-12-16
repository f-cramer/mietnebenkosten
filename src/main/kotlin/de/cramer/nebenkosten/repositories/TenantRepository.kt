package de.cramer.nebenkosten.repositories

import de.cramer.nebenkosten.entities.Tenant
import org.springframework.data.jpa.repository.JpaRepository

interface TenantRepository : JpaRepository<Tenant, Long> {

    fun findByHiddenFalse(): List<Tenant>
}
