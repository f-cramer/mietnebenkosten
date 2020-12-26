package de.cramer.nebenkosten.services

import de.cramer.nebenkosten.entities.*
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Year

@Service
class BillingService(
    private val flatService: FlatService,
    private val rentalService: RentalService,
    private val invoiceService: InvoiceService
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

    private fun LocalDatePeriod.merge(other: LocalDatePeriod) = LocalDatePeriod(
        start = minOf(this.start, other.start),
        end = if (this.end == null || other.end == null) null else maxOf(this.end, other.end)
    )

    private fun List<RentalBilling>.toBilling(rounded: Boolean): Billing = Billing(
        tenant = this[0].rental.tenant,
        period = asSequence()
            .map { it.period }
            .reduce { acc, period -> acc.merge(period) },
        entries = asSequence()
            .flatMap { it.entries }
            .groupBy { it.invoice }
            .asSequence()
            .map { it.value.merge(rounded) }
            .sorted()
            .toList()
    )

    private fun List<BillingEntry>.merge(rounded: Boolean): BillingEntry = BillingEntry(
        this[0].invoice,
        this[0].totalValue,
        if (this[0].totalValue == null) null else sumOf { it.proportionalValue ?: BigDecimal.ZERO }.takeUnless { it == BigDecimal.ZERO },
        asSequence()
            .map { it.proportionalPrice }
            .reduce { acc, price -> acc + price }
            .let { if (rounded) it.round(2, RoundingMode.UP) else it }
    )

    private fun List<Billing>.addMissingTennants(billingPeriods: List<BillingPeriod>, rounded: Boolean): List<Billing> {
        fun Tenant.getPeriod() = billingPeriods.asSequence()
            .filter { this == it.rental.tenant }
            .map { it.period }
            .reduce { acc, period -> acc.merge(period) }

        val tenants = billingPeriods.asSequence()
            .map { it.rental.tenant }
            .toSet()

        val allBillings = toMutableList()
        tenants
            .filterNot { tenant -> any { it.tenant == tenant } }
            .forEach {
                allBillings += Billing(it, it.getPeriod(), emptyList())
                    .let { billing ->  if (rounded) billing.round(2, RoundingMode.UP) else billing }
            }

        return allBillings.sorted()
    }

    private data class RentalBilling(
        val rental: Rental,
        val period: LocalDatePeriod,
        val entries: List<BillingEntry>
    )
}
