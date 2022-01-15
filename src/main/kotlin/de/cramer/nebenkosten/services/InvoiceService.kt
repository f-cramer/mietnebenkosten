package de.cramer.nebenkosten.services

import de.cramer.nebenkosten.entities.Rental as RentalEntity
import java.time.LocalDate
import de.cramer.nebenkosten.entities.ByAreaSplitAlgorithm
import de.cramer.nebenkosten.entities.ByPersonsSplitAlgorithm
import de.cramer.nebenkosten.entities.GeneralInvoice
import de.cramer.nebenkosten.entities.Invoice
import de.cramer.nebenkosten.entities.LinearSplitAlgorithm
import de.cramer.nebenkosten.entities.LocalDatePeriod
import de.cramer.nebenkosten.entities.MonetaryAmount
import de.cramer.nebenkosten.entities.RentalInvoice
import de.cramer.nebenkosten.entities.SimplePersonFallback
import de.cramer.nebenkosten.entities.SplitAlgorithm
import de.cramer.nebenkosten.entities.SplitAlgorithmType.*
import de.cramer.nebenkosten.exceptions.BadRequestException
import de.cramer.nebenkosten.exceptions.ConflictException
import de.cramer.nebenkosten.exceptions.NotFoundException
import de.cramer.nebenkosten.forms.InvoiceForm
import de.cramer.nebenkosten.forms.InvoiceType.*
import de.cramer.nebenkosten.forms.InvoiceType.Rental
import de.cramer.nebenkosten.repositories.InvoiceRepository
import org.springframework.stereotype.Service

@Service
class InvoiceService(
    private val repository: InvoiceRepository,
    private val rentalService: RentalService,
) {
    fun getInvoices(includeClosed: Boolean = false): List<Invoice> =
        (if (includeClosed) repository.findAll() else repository.findByOpenTimePeriod(LocalDate.now()))
            .sorted()

    fun getInvoicesByTimePeriod(period: LocalDatePeriod): List<Invoice> =
        (if (period.end == null)
            repository.findByOpenTimePeriod(period.start)
        else
            repository.findByTimePeriod(period.start, period.end))
            .sorted()

    fun getInvoice(id: Long): Invoice =
        repository.findById(id)
            .orElseThrow { NotFoundException() }

    fun editInvoice(id: Long, form: InvoiceForm): Invoice {
        val current = repository.findById(id)
        return if (current.isPresent) {
            val existing = current.get()
            val new = form.toInvoice(id)
            if (existing.javaClass != new.javaClass) {
                repository.delete(existing)
            }
            repository.save(new)
        } else {
            throw ConflictException()
        }
    }

    fun createInvoice(form: InvoiceForm): Invoice =
        repository.save(form.toInvoice(0))

    fun deleteInvoice(id: Long) {
        if (repository.existsById(id)) {
            repository.deleteById(id)
        } else {
            throw NotFoundException()
        }
    }

    private fun InvoiceForm.toInvoice(id: Long) = when (type) {
        General -> GeneralInvoice(
            id = id,
            description = description,
            period = LocalDatePeriod(start, end),
            price = monetaryAmount,
            order = order,
            splitAlgorithm = toSplitAlgorithm()
        )
        Rental -> RentalInvoice(
            id = id,
            description = description,
            period = LocalDatePeriod(start, end),
            price = monetaryAmount,
            order = order,
            rental = toRental()
        )
    }

    private fun InvoiceForm.toSplitAlgorithm(): SplitAlgorithm = when (splitAlgorithmType) {
        ByArea -> ByAreaSplitAlgorithm
        ByPersons -> ByPersonsSplitAlgorithm(SimplePersonFallback(0))
        Linear -> LinearSplitAlgorithm
        else -> throw BadRequestException()
    }

    private fun InvoiceForm.toRental(): RentalEntity = rental?.let { rentalService.getRental(it) } ?: throw BadRequestException()

    private val InvoiceForm.monetaryAmount: MonetaryAmount
        get() = MonetaryAmount(priceInCent) / 100
}
