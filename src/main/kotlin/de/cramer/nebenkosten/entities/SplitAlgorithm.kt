package de.cramer.nebenkosten.entities

import java.math.BigDecimal
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.*
import com.fasterxml.jackson.annotation.JsonTypeInfo
import de.cramer.nebenkosten.utils.getLengthInMonths
import de.cramer.nebenkosten.utils.toInternalBigDecimal

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes(Type(ByAreaSplitAlgorithm::class), Type(ByPersonsSplitAlgorithm::class), Type(LinearSplitAlgorithm::class))
sealed class SplitAlgorithm(
    @JsonIgnore val type: SplitAlgorithmType,
    @JsonIgnore val unit: String,
) {

    abstract fun split(invoice: Invoice, billingPeriods: Collection<BillingPeriod>): List<InvoiceSplit>

    abstract fun mergeValues(values: Collection<BigDecimal>): BigDecimal?

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SplitAlgorithm

        if (type != other.type) return false
        if (unit != other.unit) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + unit.hashCode()
        return result
    }
}

abstract class ByTimeSplitAlgorithm(type: SplitAlgorithmType, unit: String) : SplitAlgorithm(type, unit) {

    override fun split(invoice: Invoice, billingPeriods: Collection<BillingPeriod>): List<InvoiceSplit> {
        val totalLength = invoice.period.getLengthInMonths().toInternalBigDecimal()
        val total = getTotal(invoice, billingPeriods)
        return billingPeriods
            .map {
                val (splittedValue, splittedPrice) = getNewPrice(invoice, totalLength, it, total)
                InvoiceSplit(invoice, it, total, splittedValue, splittedPrice)
            }
    }

    protected abstract fun getPart(invoice: Invoice, billingPeriod: BillingPeriod): BigDecimal

    protected abstract fun getTotal(invoice: Invoice, billingPeriods: Collection<BillingPeriod>): BigDecimal

    protected open fun getNewPrice(invoice: Invoice, totalLength: BigDecimal, billingPeriod: BillingPeriod, total: BigDecimal): Pair<BigDecimal, MonetaryAmount> =
        if (billingPeriod.period.isOverlapping(invoice.period)) {
            val billingPeriodLength = billingPeriod.period.intersect(invoice.period).getLengthInMonths().toInternalBigDecimal()
            val part = getPart(invoice, billingPeriod)
            val factor = billingPeriodLength / totalLength * part / total
            Pair(part, invoice.price * factor)
        } else {
            Pair(BigDecimal.ZERO, MonetaryAmount())
        }
}

object ByAreaSplitAlgorithm : ByTimeSplitAlgorithm(SplitAlgorithmType.ByArea, "m²") {

    override fun getPart(invoice: Invoice, billingPeriod: BillingPeriod): BigDecimal = billingPeriod.rental.flat.area.toInternalBigDecimal()

    override fun getTotal(invoice: Invoice, billingPeriods: Collection<BillingPeriod>): BigDecimal = billingPeriods.asSequence()
        .map { it.rental.flat }
        .distinct()
        .sumOf { it.area }
        .toInternalBigDecimal()

    override fun mergeValues(values: Collection<BigDecimal>): BigDecimal? =
        values.firstOrNull()
}

data class ByPersonsSplitAlgorithm(
    private val personFallback: PersonFallback,
) : SplitAlgorithm(SplitAlgorithmType.ByPersons, "PM") {

    override fun split(invoice: Invoice, billingPeriods: Collection<BillingPeriod>): List<InvoiceSplit> {
        val totalPersonMonths = getTotal(invoice, billingPeriods)
        return billingPeriods
            .map {
                val (splittedValue, splittedPrice) = getNewPrice(invoice, it, totalPersonMonths)
                InvoiceSplit(invoice, it, totalPersonMonths, splittedValue, splittedPrice)
            }
    }

    override fun mergeValues(values: Collection<BigDecimal>): BigDecimal? =
        if (values.isEmpty()) null else values.sumOf { it }

    private fun getNewPrice(invoice: Invoice, billingPeriod: BillingPeriod, total: BigDecimal): Pair<BigDecimal, MonetaryAmount> =
        if (billingPeriod.period.isOverlapping(invoice.period)) {
            val part = getPart(invoice, billingPeriod)
            Pair(part, invoice.price * part / total)
        } else {
            Pair(BigDecimal.ZERO, MonetaryAmount())
        }

    private fun getPart(invoice: Invoice, billingPeriod: BillingPeriod): BigDecimal =
        (billingPeriod.rental.persons * billingPeriod.period.intersect(invoice.period).getLengthInMonths()).toInternalBigDecimal()

    private fun getTotal(invoice: Invoice, billingPeriods: Collection<BillingPeriod>): BigDecimal {
        val simpleTotal = billingPeriods.sumOf { getPart(invoice, it) }
        val additional = personFallback.getTotalFallbackTime(invoice, billingPeriods)
        return simpleTotal + additional
    }
}

object LinearSplitAlgorithm : ByTimeSplitAlgorithm(SplitAlgorithmType.Linear, "") {

    override fun getPart(invoice: Invoice, billingPeriod: BillingPeriod): BigDecimal = BigDecimal.ONE

    override fun getTotal(invoice: Invoice, billingPeriods: Collection<BillingPeriod>): BigDecimal =
        billingPeriods.asSequence()
            .map { it.rental.flat }
            .distinct()
            .count()
            .toInternalBigDecimal()

    override fun mergeValues(values: Collection<BigDecimal>): BigDecimal? =
        values.firstOrNull()
}

enum class SplitAlgorithmType {

    ByArea, ByPersons, Linear
}
