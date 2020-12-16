package de.cramer.nebenkosten.entities

import de.cramer.nebenkosten.utils.LocalDatePeriodUtils.getLengthInMonths
import kotlin.math.roundToLong

sealed class SplitAlgorithm {

    abstract fun split(bill: Bill, billings: Collection<Billing>): List<BillSplit>
}

abstract class ByTimeSplitAlgorithm : SplitAlgorithm() {

    override fun split(bill: Bill, billings: Collection<Billing>): List<BillSplit> {
        val totalLength = bill.period.getLengthInMonths()
        val total = getTotal(bill, billings)
        return billings
            .map { BillSplit(bill, it, getNewPrice(bill, totalLength, it, total)) }
    }

    protected abstract fun getPart(bill: Bill, billing: Billing): Double

    protected abstract fun getTotal(bill: Bill, billings: Collection<Billing>): Double

    protected open fun getNewPrice(bill: Bill, totalLength: Double, billing: Billing, total: Double): MonetaryAmount = if (billing.period.isOverlapping(bill.period)) {
        val billingLength = billing.period.intersect(bill.period).getLengthInMonths()
        val part = getPart(bill, billing)
        val factor = billingLength / totalLength * part / total
        MonetaryAmount((bill.price.amount * factor).roundToLong())
    } else {
        MonetaryAmount(0)
    }
}

object ByAreaSplitAlgorithm : ByTimeSplitAlgorithm() {

    override fun getPart(bill: Bill, billing: Billing): Double = billing.rental.flat.area.toDouble()

    override fun getTotal(bill: Bill, billings: Collection<Billing>): Double = billings.asSequence()
        .map { it.rental.flat }
        .distinct()
        .sumOf { it.area }
        .toDouble()
}

data class ByPersonsSplitAlgorithm(
    private val personFallback: PersonFallback
) : SplitAlgorithm() {

    override fun split(bill: Bill, billings: Collection<Billing>): List<BillSplit> {
        val totalPersonMonths = getTotal(bill, billings)
        return billings
            .map { BillSplit(bill, it, getNewPrice(bill, it, totalPersonMonths)) }
    }

    private fun getNewPrice(bill: Bill, billing: Billing, total: Double): MonetaryAmount = if (billing.period.isOverlapping(bill.period)) {
        val factor = getPart(bill, billing) / total
        bill.price * factor
        MonetaryAmount((bill.price.amount * factor).roundToLong())
    } else {
        MonetaryAmount(0)
    }

    private fun getPart(bill: Bill, billing: Billing): Double = billing.rental.persons.toLong() * billing.period.intersect(bill.period).getLengthInMonths()

    private fun getTotal(bill: Bill, billings: Collection<Billing>): Double {
        val simpleTotal = billings.sumOf { getPart(bill, it) }
        val additional = personFallback.getTotalFallbackTime(bill, billings)
        return simpleTotal + additional
    }
}

object LinearSplitAlgorithm : ByTimeSplitAlgorithm() {

    override fun getPart(bill: Bill, billing: Billing): Double = 1.toDouble()

    override fun getTotal(bill: Bill, billings: Collection<Billing>): Double = billings.asSequence()
        .map { it.rental.flat }
        .distinct()
        .count()
        .toDouble()
}
