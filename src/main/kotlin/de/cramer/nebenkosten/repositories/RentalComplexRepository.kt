package de.cramer.nebenkosten.repositories

import de.cramer.nebenkosten.entities.RentalComplex
import org.springframework.data.jpa.repository.JpaRepository

interface RentalComplexRepository : JpaRepository<RentalComplex, Long>
