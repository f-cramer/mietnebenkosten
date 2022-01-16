package de.cramer.nebenkosten.repositories

import de.cramer.nebenkosten.entities.Rental
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface RentalRepository : JpaRepository<Rental, Long>, JpaSpecificationExecutor<Rental>
