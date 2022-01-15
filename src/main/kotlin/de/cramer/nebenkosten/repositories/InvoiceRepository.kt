package de.cramer.nebenkosten.repositories

import java.time.LocalDate
import de.cramer.nebenkosten.entities.Invoice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface InvoiceRepository : JpaRepository<Invoice, Long> {

    @Query(
        """
                FROM Invoice i JOIN i.period period
                WHERE
                        (period.end IS NULL) OR
                        (period.end > :start)
            """
    )
    fun findByOpenTimePeriod(start: LocalDate): List<Invoice>

    @Query(
        """
                FROM Invoice i JOIN i.period period
                WHERE
                    (period.end IS NULL AND :end > period.start) OR
                    (period.start <= :start AND :start < period.end) OR 
                    (:start <= period.start AND period.start < :end) OR 
                    (period.start < :end AND :end <= period.end) OR
                    (:start < period.end AND period.end <= :end)
            """
    )
    fun findByTimePeriod(start: LocalDate, end: LocalDate): List<Invoice>
}
