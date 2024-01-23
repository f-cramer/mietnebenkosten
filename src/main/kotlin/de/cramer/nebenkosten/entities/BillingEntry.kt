package de.cramer.nebenkosten.entities

import java.math.BigDecimal
import java.text.NumberFormat

data class BillingEntry(
    val invoice: Invoice,
    val totalValue: BigDecimal?,
    val proportionalValue: BigDecimal?,
    val proportionalPrice: MonetaryAmount,
) : Comparable<BillingEntry> {

    override fun compareTo(other: BillingEntry): Int =
        COMPARATOR.compare(this, other)

    override fun toString(): String {
        return "(${invoice.description} ${proportionalPrice.amount})"
    }

    fun formatTotalValue(format: NumberFormat): String? {
        return totalValue?.let { format.format(it) }
    }

    fun formatProportionalValue(format: NumberFormat): String? {
        return proportionalValue?.let { format.format(it) }
    }

    companion object {

        private val COMPARATOR = compareBy<BillingEntry> { it.invoice }
            .thenBy { it.proportionalPrice }
    }
}
