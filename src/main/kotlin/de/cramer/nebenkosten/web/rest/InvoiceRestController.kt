package de.cramer.nebenkosten.web.rest

import de.cramer.nebenkosten.entities.Invoice
import de.cramer.nebenkosten.forms.InvoiceForm
import de.cramer.nebenkosten.services.InvoiceService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/invoices")
class InvoiceRestController(
    private val service: InvoiceService
) {

    @GetMapping
    fun getAllInvoices(): List<Invoice> = service.getInvoices()

    @PutMapping
    fun createInvoice(@RequestBody form: InvoiceForm): Invoice = service.createInvoice(form)

    @GetMapping("{id}")
    fun getInvoiceById(@PathVariable("id") id: Long): Invoice = service.getInvoice(id)

    @PostMapping("{id}")
    fun editInvoice(@PathVariable("id") id: Long, @RequestBody form: InvoiceForm): Invoice = service.editInvoice(id, form)

    @DeleteMapping("{id}")
    fun deleteInvoice(@PathVariable("id") id: Long): Unit = service.deleteInvoice(id)
}
