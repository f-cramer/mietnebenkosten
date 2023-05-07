package de.cramer.nebenkosten.repositories

import de.cramer.nebenkosten.entities.Tenant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface TenantRepository : JpaRepository<Tenant, Long>, JpaSpecificationExecutor<Tenant>
