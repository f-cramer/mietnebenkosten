package de.cramer.nebenkosten.services

import de.cramer.nebenkosten.entities.Address
import de.cramer.nebenkosten.entities.Billing
import de.cramer.nebenkosten.entities.BillingEntry
import de.cramer.nebenkosten.entities.BillingPeriod
import de.cramer.nebenkosten.entities.Contract
import de.cramer.nebenkosten.entities.ContractInvoice
import de.cramer.nebenkosten.entities.Flat
import de.cramer.nebenkosten.entities.FormOfAddress
import de.cramer.nebenkosten.entities.Gender
import de.cramer.nebenkosten.entities.GeneralInvoice
import de.cramer.nebenkosten.entities.Invoice
import de.cramer.nebenkosten.entities.InvoiceSplit
import de.cramer.nebenkosten.entities.Landlord
import de.cramer.nebenkosten.entities.LocalDatePeriod
import de.cramer.nebenkosten.entities.Tenant
import de.cramer.nebenkosten.exceptions.NoLandlordFoundException
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.Year
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong

@Service
class BillingService(
    private val flatService: FlatService,
    private val contractService: ContractService,
    private val invoiceService: InvoiceService,
    private val landlordService: LandlordService,
    private val messageSource: MessageSource,
) {
    fun createBillings(year: Year, locale: Locale, rounded: Boolean = false): List<Billing> = createBillings(LocalDatePeriod.ofYear(year), locale, rounded)

    fun createBillings(period: LocalDatePeriod, locale: Locale, rounded: Boolean = false): List<Billing> {
        val landlords = landlordService.getLandlordsByTimePeriod(period)

        val landlord = landlords.minOrNull() ?: run {
            val landlordNotFound = messageSource.getMessage("error.notFound.landlord", null, locale)
            throw NoLandlordFoundException(landlordNotFound)
        }
        val invoices = invoiceService.getInvoicesByTimePeriod(period)
        val billingPeriods = getBillingPeriods(period)
            .addVacancies(period, locale)

        return invoices.asSequence()
            .flatMap { it.splitByContract(billingPeriods) }
            .groupBy { it.billing.contract }
            .asSequence()
            .map { it.toBilling(period.intersect(it.key.period)) }
            .groupBy { it.contract.tenant }
            .map { it.value.toBilling(landlord, rounded) }
            .addMissingTennants(landlord, billingPeriods, rounded)
            .sorted()
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
        value.mapNotNull { it.toBillingEntry() },
    )

    private fun InvoiceSplit.toBillingEntry(): BillingEntry? =
        if (splittedValue != null && splittedValue.compareTo(BigDecimal.ZERO) == 0 && splittedAmount.amount.compareTo(BigDecimal.ZERO) == 0) {
            null
        } else {
            BillingEntry(invoice, totalValue, splittedValue, splittedAmount)
        }

    private fun Sequence<LocalDatePeriod>.merge(): List<LocalDatePeriod> {
        val result = mutableListOf<LocalDatePeriod>()

        var start: LocalDate? = null
        var end: LocalDate? = null
        for (period in sorted().toList()) {
            if (start == null) {
                start = period.start
                end = period.end
            } else if (end != null && start <= end.plusDays(1)) {
                end = if (period.end == null) {
                    null
                } else {
                    maxOf(end, period.end)
                }
            } else {
                result += LocalDatePeriod(start, end)
                start = null
                end = null
            }

            if (start != null && end == null) {
                result += LocalDatePeriod(start, null)
                break
            }
        }

        if (start != null) {
            result += LocalDatePeriod(start, end)
        }

        return result
    }

    private fun List<ContractBilling>.toBilling(landlord: Landlord, rounded: Boolean): Billing = Billing(
        landlord = landlord,
        tenant = this[0].contract.tenant,
        periods = asSequence()
            .map { it.period }
            .merge(),
        entries = asSequence()
            .flatMap { it.entries }
            .groupBy { it.invoice }
            .asSequence()
            .map { it.value.mergeSameInvoice(rounded) }
            .sorted()
            .toList(),
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
                .let { if (rounded) it.round(2, RoundingMode.UP) else it },
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

        return allBillings
    }

    private fun List<BillingPeriod>.addVacancies(period: LocalDatePeriod, locale: Locale): List<BillingPeriod> {
        val tenantId = AtomicLong()
        val list = if (this is MutableList<BillingPeriod>) this else toMutableList()
        list += groupBy({ it.contract.flat }) { it.period }
            .mapValues { (_, v) -> v.toSet() }
            .flatMap { (flat, periods) -> getVacancies(flat, periods, period, locale, tenantId) }
        return list
    }

    private fun getVacancies(flat: Flat, billingPeriods: Set<LocalDatePeriod>, period: LocalDatePeriod, locale: Locale, tenantId: AtomicLong): List<BillingPeriod> {
        val vacancyName = messageSource.getMessage("vacancy", null, locale)
        val tenant by lazy {
            // only create the tenant (and use an id) when necessary
            Tenant(tenantId.decrementAndGet(), vacancyName, flat.name, Address.EMPTY, Gender.MALE, FormOfAddress.FORMAL, flat.rentalComplex, true, generated = true)
        }

        return buildList {
            var previousPeriod = period.start.minusDays(1).let {
                LocalDatePeriod(it, it)
            }
            for (p in billingPeriods.sorted()) {
                val previousEnd = previousPeriod.end ?: break

                val nextPeriodStart = previousEnd.plusDays(1)
                if (nextPeriodStart < p.start) {
                    this += LocalDatePeriod(nextPeriodStart, p.start.minusDays(1))
                }
                previousPeriod = LocalDatePeriod(p.start, if (p.end == null) null else maxOf(previousEnd, p.end))
            }

            val previousEnd = previousPeriod.end
            if (previousEnd != null && (period.end == null || previousEnd < period.end)) {
                this += LocalDatePeriod(previousEnd.plusDays(1), period.end)
            }
        }.map {
            BillingPeriod(Contract(flat, tenant, 0, it), it)
        }
    }

    private data class ContractBilling(
        val contract: Contract,
        val period: LocalDatePeriod,
        val entries: List<BillingEntry>,
    )
}
