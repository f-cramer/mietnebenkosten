package de.cramer.nebenkosten.services

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.Year
import de.cramer.nebenkosten.entities.Billing
import de.cramer.nebenkosten.entities.BillingEntry
import de.cramer.nebenkosten.entities.BillingPeriod
import de.cramer.nebenkosten.entities.GeneralInvoice
import de.cramer.nebenkosten.entities.Invoice
import de.cramer.nebenkosten.entities.InvoiceSplit
import de.cramer.nebenkosten.entities.LocalDatePeriod
import de.cramer.nebenkosten.entities.Rental
import de.cramer.nebenkosten.entities.RentalInvoice
import de.cramer.nebenkosten.entities.Tenant
import org.springframework.stereotype.Service

@Service
class BillingService(
    private val flatService: FlatService,
    private val rentalService: RentalService,
    private val invoiceService: InvoiceService,
) {
    fun createBillings(year: Year, rounded: Boolean = false): List<Billing> = createBillings(LocalDatePeriod.ofYear(year), rounded)

    fun createBillings(period: LocalDatePeriod, rounded: Boolean = false): List<Billing> {
        val invoices = invoiceService.getInvoicesByTimePeriod(period)
        val billingPeriods = getBillingPeriods(period)

        return invoices.asSequence()
            .flatMap { it.splitByRental(billingPeriods) }
            .groupBy { it.billing.rental }
            .asSequence()
            .map { it.toBilling(period.intersect(it.key.period)) }
            .groupBy { it.rental.tenant }
            .map { it.value.toBilling(rounded) }
            .addMissingTennants(billingPeriods, rounded)
    }

    private fun getBillingPeriods(period: LocalDatePeriod): List<BillingPeriod> = flatService.getFlats().asSequence()
        .flatMap { rentalService.getRentalsByFlatAndPeriod(it, period) }
        .map { BillingPeriod(it, period.intersect(it.period)) }
        .toList()

    private fun Invoice.splitByRental(billingPeriods: List<BillingPeriod>): List<InvoiceSplit> = when (this) {
        is GeneralInvoice -> splitAlgorithm.split(this, billingPeriods)
        is RentalInvoice -> listOf(InvoiceSplit(this, BillingPeriod(rental, period.intersect(rental.period)), null, null, price))
    }

    private fun Map.Entry<Rental, List<InvoiceSplit>>.toBilling(period: LocalDatePeriod): RentalBilling = RentalBilling(
        key,
        period,
        value.map { it.toBillingEntry() }
    )

    private fun InvoiceSplit.toBillingEntry(): BillingEntry = BillingEntry(invoice, totalValue, splittedValue, splittedAmount)

    private fun Sequence<LocalDatePeriod>.merge(): LocalDatePeriod {
        var start: LocalDate? = null
        var end: LocalDate? = null
        for (period in this) {
            end = if (start == null) {
                period.end
            } else if (end == null || period.end == null) {
                null
            } else {
                maxOf(end, period.end)
            }
            start = if (start == null) {
                period.start
            } else {
                minOf(start, period.start)
            }
        }

        if (start == null) {
            throw IllegalArgumentException("merge() can only be called on non empty instances of Iterable")
        }
        return LocalDatePeriod(start, end)
    }

    private fun List<RentalBilling>.toBilling(rounded: Boolean): Billing = Billing(
        tenant = this[0].rental.tenant,
        period = asSequence()
            .map { it.period }
            .merge(),
        entries = asSequence()
            .flatMap { it.entries }
            .groupBy { it.invoice }
            .asSequence()
            .map { it.value.mergeSameInvoice(rounded) }
            .sorted()
            .toList()
    )

    private fun List<BillingEntry>.mergeSameInvoice(rounded: Boolean): BillingEntry = first().let { first ->
        val invoice = first.invoice
        BillingEntry(
            invoice,
            first.totalValue,
            if (first.totalValue == null) null else invoice.mergeValues(this),
            asSequence()
                .map { it.proportionalPrice }
                .reduce { acc, price -> acc + price }
                .let { if (rounded) it.round(2, RoundingMode.UP) else it }
        )
    }

    private fun Invoice.mergeValues(billingEntries: List<BillingEntry>): BigDecimal? = when (this) {
        is GeneralInvoice -> splitAlgorithm.mergeValues(billingEntries.mapNotNull { it.proportionalValue })
        is RentalInvoice -> null
    }

    private fun List<Billing>.addMissingTennants(billingPeriods: List<BillingPeriod>, rounded: Boolean): List<Billing> {
        fun Tenant.getPeriod() = billingPeriods.asSequence()
            .filter { this == it.rental.tenant }
            .map { it.period }
            .merge()

        val tenants = billingPeriods.asSequence()
            .map { it.rental.tenant }
            .toSet()

        val allBillings = toMutableList()
        tenants
            .filterNot { tenant -> any { it.tenant == tenant } }
            .forEach {
                allBillings += Billing(it, it.getPeriod(), emptyList())
                    .let { billing -> if (rounded) billing.round(2, RoundingMode.UP) else billing }
            }

        return allBillings.sorted()
    }

    private data class RentalBilling(
        val rental: Rental,
        val period: LocalDatePeriod,
        val entries: List<BillingEntry>,
    )
}
