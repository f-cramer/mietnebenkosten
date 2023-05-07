package de.cramer.nebenkosten.repositories

import de.cramer.nebenkosten.entities.Flat
import org.springframework.data.jpa.repository.JpaRepository

interface FlatRepository : JpaRepository<Flat, Long>
