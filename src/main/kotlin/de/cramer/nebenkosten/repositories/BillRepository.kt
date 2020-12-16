package de.cramer.nebenkosten.repositories

import de.cramer.nebenkosten.entities.Bill
import org.springframework.data.jpa.repository.JpaRepository

interface BillRepository : JpaRepository<Bill, Long>
