package de.cramer.nebenkosten.services

import de.cramer.nebenkosten.entities.Bill
import de.cramer.nebenkosten.entities.LocalDatePeriod
import de.cramer.nebenkosten.entities.MonetaryAmount
import de.cramer.nebenkosten.exceptions.ConflictException
import de.cramer.nebenkosten.exceptions.NotFoundException
import de.cramer.nebenkosten.forms.BillForm
import de.cramer.nebenkosten.repositories.BillRepository
import org.springframework.stereotype.Service

@Service
class BillService(
    private val repository: BillRepository
) {
    fun getBills(): List<Bill> = repository.findAll()

    fun getBill(id: Long): Bill = repository.findById(id)
        .orElseThrow { NotFoundException() }

    fun editBill(id: Long, form: BillForm): Bill =
        if (repository.existsById(id)) {
            repository.save(form.toBill().copy(id = id))
        } else {
            throw ConflictException()
        }

    fun createBill(form: BillForm): Bill =
        repository.save(form.toBill())

    fun deleteBill(id: Long) {
        if (repository.existsById(id)) {
            repository.deleteById(id)
        } else {
            throw NotFoundException()
        }
    }

    fun BillForm.toBill() = Bill(
        description = description,
        period = LocalDatePeriod(start, end),
        price = MonetaryAmount(priceInCent)
    )
}
