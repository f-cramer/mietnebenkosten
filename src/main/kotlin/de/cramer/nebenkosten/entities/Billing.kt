package de.cramer.nebenkosten.entities

import java.math.RoundingMode
import kotlin.math.min

data class Billing(
    val landlord: Landlord,
    val tenant: Tenant,
    val periods: List<LocalDatePeriod>,
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
        scale = scale,
    )

    fun roundUp(scale: Int): Billing = round(scale, RoundingMode.UP)

    companion object {
        private val PERIOD_COMPARATOR = compareBy<LocalDatePeriod> { it.start }.thenBy { it.end }
        private val PERIODS_COMPARATOR: Comparator<List<LocalDatePeriod>> = Comparator { o1, o2 ->
            for (i in 0..min(o1.size, o2.size)) {
                val result = PERIOD_COMPARATOR.compare(o1[i], o2[i])
                if (result != 0) {
                    return@Comparator result
                }
            }
            o1.size.compareTo(o2.size)
        }

        private val COMPARATOR = compareBy<Billing> { it.tenant }
            .thenBy(PERIODS_COMPARATOR) { it.periods }
    }
}
