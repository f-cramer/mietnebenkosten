package de.cramer.nebenkosten.entities

import de.cramer.nebenkosten.utils.LocalDatePeriodUtils.getLengthInMonths

sealed class PersonFallback {

    abstract fun getTotalFallbackTime(bill: Bill, billings: Collection<Billing>): Double
}

data class SimplePersonFallback(
    private val persons: Int = 1
) : PersonFallback() {

    override fun getTotalFallbackTime(bill: Bill, billings: Collection<Billing>): Double {
        val totalLengthInMonths = bill.period.getLengthInMonths()
        return persons * billings
            .groupBy { it.rental.flat }
            .values
            .asSequence()
            .map { b -> totalLengthInMonths - b.sumOf { it.period.getLengthInMonths() } }
            .filter { it > 0 }
            .sum()
    }
}
