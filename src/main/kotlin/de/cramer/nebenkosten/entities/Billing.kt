package de.cramer.nebenkosten.entities

data class Billing(
    val rental: Rental,
    val period: LocalDatePeriod
) : Comparable<Billing> {
    override fun compareTo(other: Billing): Int = COMPARATOR.compare(this, other)

    companion object {

        private val COMPARATOR = Comparator.comparing<Billing, Rental> { it.rental }
            .thenComparing<LocalDatePeriod> { it.period }
    }
}
