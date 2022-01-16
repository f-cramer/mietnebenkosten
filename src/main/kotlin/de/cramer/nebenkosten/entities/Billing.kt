package de.cramer.nebenkosten.entities

import java.math.RoundingMode

data class Billing(
    val landlord: Landlord,
    val tenant: Tenant,
    val period: LocalDatePeriod,
    val entries: List<BillingEntry>,
    val scale: Int? = null,
) : Comparable<Billing> {
    override fun compareTo(other: Billing): Int = COMPARATOR.compare(this, other)

    val total: MonetaryAmount by lazy {
        entries.asSequence()
            .map { it.proportionalPrice }
            .reduceOrNull { acc, monetaryAmount -> acc + monetaryAmount } ?: MonetaryAmount().run { if (scale == null) this else this.round(scale, RoundingMode.UNNECESSARY) }
    }

    fun round(scale: Int, roundingMode: RoundingMode): Billing = copy(
        entries = entries.map { it.copy(proportionalPrice = it.proportionalPrice.round(scale, roundingMode)) },
        scale = scale
    )

    fun roundUp(scale: Int): Billing = round(scale, RoundingMode.UP)

    companion object {

        private val COMPARATOR = compareBy<Billing> { it.tenant }
            .thenBy { it.period }
    }
}
