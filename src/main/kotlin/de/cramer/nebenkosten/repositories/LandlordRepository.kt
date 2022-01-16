package de.cramer.nebenkosten.repositories

import de.cramer.nebenkosten.entities.Landlord
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface LandlordRepository : JpaRepository<Landlord, Long>, JpaSpecificationExecutor<Landlord>
