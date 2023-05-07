package de.cramer.nebenkosten.web.rest

import de.cramer.nebenkosten.entities.Invoice
import de.cramer.nebenkosten.entities.RentalComplex
import de.cramer.nebenkosten.forms.InvoiceForm
import de.cramer.nebenkosten.services.InvoiceService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/invoices")
class InvoiceRestController(
    private val service: InvoiceService,
) {

    @GetMapping
    fun getAllInvoices(): List<Invoice> = service.getInvoices()

    @PutMapping
    fun createInvoice(@RequestBody form: InvoiceForm, rentalComplex: RentalComplex): Invoice = service.createInvoice(form, rentalComplex)

    @GetMapping("{id}")
    fun getInvoiceById(@PathVariable("id") id: Long): Invoice = service.getInvoice(id)

    @PostMapping("{id}")
    fun editInvoice(@PathVariable("id") id: Long, @RequestBody form: InvoiceForm, rentalComplex: RentalComplex): Invoice = service.editInvoice(id, form, rentalComplex)

    @DeleteMapping("{id}")
    fun deleteInvoice(@PathVariable("id") id: Long): Unit = service.deleteInvoice(id)
}
