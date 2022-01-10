package de.cramer.nebenkosten.repositories

import java.time.LocalDate
import de.cramer.nebenkosten.entities.Flat
import de.cramer.nebenkosten.entities.Rental
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface RentalRepository : JpaRepository<Rental, Long> {

    @Query(
        """
                FROM Rental r JOIN r.period period
                WHERE
                    (period.end IS NULL) OR
                    (period.end > :start)
            """
    )
    fun findByOpenTimePeriod(start: LocalDate): List<Rental>

    @Query(
        """
                FROM Rental r JOIN r.period period
                WHERE
                    (period.end IS NULL AND :end > period.start) OR
                    (period.start <= :start AND :start < period.end) OR 
                    (:start <= period.start AND period.start < :end) OR 
                    (period.start < :end AND :end <= period.end) OR
                    (:start < period.end AND period.end <= :end)
            """
    )
    fun findByTimePeriod(start: LocalDate, end: LocalDate): List<Rental>

    @Query(
        """
                FROM Rental r JOIN r.period period
                WHERE
                    r.flat = :flat AND
                    (
                    (period.end IS NULL) OR
                    (period.end > :start)
                    )
            """
    )
    fun findByFlatAndOpenTimePeriod(flat: Flat, start: LocalDate): List<Rental>

    @Query(
        """
                FROM Rental r JOIN r.period period
                WHERE
                    r.flat = :flat AND
                    (
                    (period.end IS NULL AND :end > period.start) OR
                    (period.start <= :start AND :start < period.end) OR 
                    (:start <= period.start AND period.start < :end) OR 
                    (period.start < :end AND :end <= period.end) OR
                    (:start < period.end AND period.end <= :end)
                    )
            """
    )
    fun findByFlatAndTimePeriod(flat: Flat, start: LocalDate, end: LocalDate): List<Rental>

    fun findByPeriodEndIsNullOrPeriodEndGreaterThanEqual(today: LocalDate): List<Rental>
}
