package de.cramer.nebenkosten.repositories

import de.cramer.nebenkosten.entities.Flat
import de.cramer.nebenkosten.entities.Rental
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface RentalRepository : JpaRepository<Rental, Long> {

    @Query(
        """
                FROM Rental r JOIN r.period period
                WHERE
                    r.flat = :flat AND
                    (
                    (:end IS NULL AND period.end IS NULL) OR
                    (:end IS NULL AND period.end > :start) OR
                    (period.end IS NULL AND :end > period.start) OR
                    (period.start <= :start AND :start < period.end) OR 
                    (:start <= period.start AND period.start < :end) OR 
                    (period.start < :end AND :end <= period.end) OR
                    (:start < period.end AND period.end <= :end)
                    )
            """
    )
    fun findByFlatAndTimePeriod(flat: Flat, start: LocalDate, end: LocalDate?): List<Rental>

    fun findByPeriodEndIsNullOrPeriodEndGreaterThanEqual(today: LocalDate): List<Rental>
}
