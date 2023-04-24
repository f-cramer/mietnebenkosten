package de.cramer.nebenkosten.services

import de.cramer.nebenkosten.entities.Billing
import de.cramer.nebenkosten.entities.BillingEntry
import de.cramer.nebenkosten.entities.BillingPeriod
import de.cramer.nebenkosten.entities.Contract
import de.cramer.nebenkosten.entities.ContractInvoice
import de.cramer.nebenkosten.entities.GeneralInvoice
import de.cramer.nebenkosten.entities.Invoice
import de.cramer.nebenkosten.entities.InvoiceSplit
import de.cramer.nebenkosten.entities.Landlord
import de.cramer.nebenkosten.entities.LocalDatePeriod
import de.cramer.nebenkosten.entities.Tenant
import de.cramer.nebenkosten.exceptions.NoLandlordFoundException
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.Year

@Service
class BillingService(
    private val flatService: FlatService,
    private val contractService: ContractService,
    private val invoiceService: InvoiceService,
    private val landlordService: LandlordService,
) {
    fun createBillings(year: Year, rounded: Boolean = false): List<Billing> = createBillings(LocalDatePeriod.ofYear(year), rounded)

    fun createBillings(period: LocalDatePeriod, rounded: Boolean = false): List<Billing> {
        val landlords = landlordService.getLandlordsByTimePeriod(period)

        val landlord = landlords.minOrNull() ?: throw NoLandlordFoundException()
        val invoices = invoiceService.getInvoicesByTimePeriod(period)
        val billingPeriods = getBillingPeriods(period)

        return invoices.asSequence()
            .flatMap { it.splitByContract(billingPeriods) }
            .groupBy { it.billing.contract }
            .asSequence()
            .map { it.toBilling(period.intersect(it.key.period)) }
            .groupBy { it.contract.tenant }
            .map { it.value.toBilling(landlord, rounded) }
            .addMissingTennants(landlord, billingPeriods, rounded)
    }

    private fun getBillingPeriods(period: LocalDatePeriod): List<BillingPeriod> = flatService.getFlats().asSequence()
        .flatMap { contractService.getContractsByFlatAndPeriod(it, period) }
        .map { BillingPeriod(it, period.intersect(it.period)) }
        .toList()

    private fun Invoice.splitByContract(billingPeriods: List<BillingPeriod>): List<InvoiceSplit> = when (this) {
        is GeneralInvoice -> splitAlgorithm.split(this, billingPeriods)
        is ContractInvoice -> listOf(InvoiceSplit(this, BillingPeriod(contract, period.intersect(contract.period)), null, null, price))
        else -> error("unsupported invoice type $this")
    }

    private fun Map.Entry<Contract, List<InvoiceSplit>>.toBilling(period: LocalDatePeriod): ContractBilling = ContractBilling(
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

    private fun List<ContractBilling>.toBilling(landlord: Landlord, rounded: Boolean): Billing = Billing(
        landlord = landlord,
        tenant = this[0].contract.tenant,
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
        is ContractInvoice -> null
        else -> error("unsupported invoice type $this")
    }

    private fun List<Billing>.addMissingTennants(landlord: Landlord, billingPeriods: List<BillingPeriod>, rounded: Boolean): List<Billing> {
        fun Tenant.getPeriod() = billingPeriods.asSequence()
            .filter { this == it.contract.tenant }
            .map { it.period }
            .merge()

        val tenants = billingPeriods.asSequence()
            .map { it.contract.tenant }
            .toSet()

        val allBillings = toMutableList()
        tenants
            .filterNot { tenant -> any { it.tenant == tenant } }
            .forEach {
                allBillings += Billing(landlord, it, it.getPeriod(), emptyList())
                    .let { billing -> if (rounded) billing.round(2, RoundingMode.UP) else billing }
            }

        return allBillings.sorted()
    }

    private data class ContractBilling(
        val contract: Contract,
        val period: LocalDatePeriod,
        val entries: List<BillingEntry>,
    )
}
