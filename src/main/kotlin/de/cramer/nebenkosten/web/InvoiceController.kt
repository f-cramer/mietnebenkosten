package de.cramer.nebenkosten.web

import java.time.LocalDate
import java.time.Year
import java.time.temporal.TemporalAdjusters
import de.cramer.nebenkosten.entities.GeneralInvoice
import de.cramer.nebenkosten.entities.LocalDatePeriod
import de.cramer.nebenkosten.entities.RentalInvoice
import de.cramer.nebenkosten.entities.SplitAlgorithmType
import de.cramer.nebenkosten.exceptions.BadRequestException
import de.cramer.nebenkosten.extensions.set
import de.cramer.nebenkosten.forms.InvoiceForm
import de.cramer.nebenkosten.forms.InvoiceType
import de.cramer.nebenkosten.services.InvoiceService
import de.cramer.nebenkosten.services.RentalService
import org.slf4j.Logger
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.SessionAttribute
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("invoices")
class InvoiceController(
    private val log: Logger,
    private val invoiceService: InvoiceService,
    private val rentalService: RentalService,
) {

    @GetMapping("")
    fun getInvoices(
        @RequestParam(name = "includeClosed", defaultValue = "false") includeClosed: Boolean,
        year: Year,
        model: Model,
    ): String {
        model["includeClosed"] = includeClosed
        model["invoices"] = invoiceService.getInvoicesByTimePeriod(LocalDatePeriod.ofYear(year))
        return "invoices"
    }

    @GetMapping("create")
    fun createInvoice(
        year: Year,
        model: Model,
    ): String {
        val firstDayOfYear = year.atDay(1)
        model["invoiceTypes"] = de.cramer.nebenkosten.entities.InvoiceType.values()
        model["splitAlgorithmTypes"] = SplitAlgorithmType.values()
        model["rentals"] = rentalService.getRentalsByPeriod(LocalDatePeriod.ofYear(year))
        model["defaultStart"] = firstDayOfYear
        model["defaultEnd"] = firstDayOfYear.with(TemporalAdjusters.lastDayOfYear())
        return "invoice"
    }

    @PostMapping("create")
    fun createInvoice(
        @SessionAttribute
        @RequestParam("description") description: String,
        @RequestParam("price") price: Long,
        @RequestParam("type") type: InvoiceType,
        @RequestParam("splitAlgorithm", required = false) splitAlgorithmType: SplitAlgorithmType?,
        @RequestParam("rental", required = false) rental: Long?,
        @RequestParam("order") order: Int,
        @RequestParam("start") start: LocalDate,
        @RequestParam("end", required = false) end: LocalDate?,
        redirectAttributes: RedirectAttributes,
    ): String = try {
        InvoiceForm(description, price, type, splitAlgorithmType, rental, order, start, end).apply {
            validate()
            invoiceService.createInvoice(this)
        }
        "redirect:/invoices"
    } catch (e: Exception) {
        log.error(e.message, e)
        redirectAttributes["error"] = "create"
        redirectAttributes["errorMessage"] = e.message ?: ""
        "redirect:/invoices"
    }

    @GetMapping("show/{id}")
    fun getInvoice(
        @PathVariable("id") id: Long,
        @RequestParam("error", defaultValue = "[]") error: List<String>,
        year: Year,
        model: Model,
    ): String {
        val invoice = invoiceService.getInvoice(id)
        model["invoice"] = invoice
        model["invoiceTypes"] = de.cramer.nebenkosten.entities.InvoiceType.values()
        model["splitAlgorithmTypes"] = SplitAlgorithmType.values()
        model["rentals"] = rentalService.getRentalsByPeriod(LocalDatePeriod.ofYear(year))
        model["selectedSplitAlgorithmType"] = (invoice as? GeneralInvoice)?.splitAlgorithm?.type ?: ""
        model["selectedRental"] = (invoice as? RentalInvoice)?.rental ?: ""
        return "invoice"
    }

    @PostMapping("edit/{id}")
    fun editInvoice(
        @PathVariable("id") id: Long,
        @RequestParam("description") description: String,
        @RequestParam("price") price: Long,
        @RequestParam("type") type: InvoiceType,
        @RequestParam("splitAlgorithm", required = false) splitAlgorithmType: SplitAlgorithmType?,
        @RequestParam("rental", required = false) rental: Long?,
        @RequestParam("order") order: Int,
        @RequestParam("start") start: LocalDate,
        @RequestParam("end", required = false) end: LocalDate?,
        redirectAttributes: RedirectAttributes,
    ): String = try {
        InvoiceForm(description, price, type, splitAlgorithmType, rental, order, start, end).apply {
            validate()
            invoiceService.editInvoice(id, this)
        }
        "redirect:/invoices"
    } catch (e: BadRequestException) {
        log.debug(e.message, e)
        redirectAttributes["error"] = "badRequest"
        redirectAttributes["errorMessage"] = e.message ?: ""
        "redirect:/invoices/show/$id"
    } catch (e: Exception) {
        log.error(e.message, e)
        redirectAttributes["error"] = "edit"
        redirectAttributes["errorMessage"] = e.message ?: ""
        "redirect:/invoices/show/$id"
    }

    @PostMapping("delete/{id}")
    fun alterInvoice(
        @PathVariable("id") id: Long,
        redirectAttributes: RedirectAttributes,
    ): String = try {
        invoiceService.deleteInvoice(id)
        "redirect:/invoices"
    } catch (e: Exception) {
        log.error(e.message, e)
        redirectAttributes["error"] = "delete"
        redirectAttributes["errorMessage"] = e.message ?: ""
        "redirect:/invoices/show/$id"
    }
}
