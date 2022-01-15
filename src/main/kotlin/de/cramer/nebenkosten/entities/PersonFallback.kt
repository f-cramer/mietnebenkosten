package de.cramer.nebenkosten.entities

import java.math.BigDecimal
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import de.cramer.nebenkosten.extensions.getLengthInMonths
import de.cramer.nebenkosten.extensions.toInternalBigDecimal

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes(JsonSubTypes.Type(SimplePersonFallback::class))
sealed class PersonFallback {

    abstract fun getTotalFallbackTime(invoice: Invoice, billingPeriods: Collection<BillingPeriod>): BigDecimal
}

data class SimplePersonFallback(
    private val persons: Int = 1,
) : PersonFallback() {

    override fun getTotalFallbackTime(invoice: Invoice, billingPeriods: Collection<BillingPeriod>): BigDecimal {
        val totalLengthInMonths = invoice.period.getLengthInMonths()
        return (persons * billingPeriods
            .groupBy { it.rental.flat }
            .values
            .asSequence()
            .map { b -> totalLengthInMonths - b.sumOf { it.period.getLengthInMonths() } }
            .filter { it > 0 }
            .sum())
            .toInternalBigDecimal()
    }
}
