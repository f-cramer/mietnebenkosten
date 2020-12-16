package de.cramer.nebenkosten.services

import de.cramer.nebenkosten.entities.Billing
import de.cramer.nebenkosten.entities.Flat
import de.cramer.nebenkosten.entities.LocalDatePeriod
import org.springframework.stereotype.Service
import java.time.Year

@Service
class BillingService(
    private val rentalService: RentalService,
) {
    fun createBilling(flat: Flat, year: Year): List<Billing> {
        val period = LocalDatePeriod.ofYear(year)
        return createBilling(flat, period)
    }

    fun createBilling(flat: Flat, period: LocalDatePeriod): List<Billing> {
        val rentals = rentalService.getRentalsByFlatAndPeriod(flat, period)
        return rentals.asSequence()
//            .filter { period.isOverlapping(it.period) }
            .map { Billing(it, period.intersect(it.period)) }
            .toList()
    }
}
