package de.cramer.nebenkosten.web

import de.cramer.nebenkosten.entities.ContractInvoice
import de.cramer.nebenkosten.entities.GeneralInvoice
import de.cramer.nebenkosten.entities.LocalDatePeriod
import de.cramer.nebenkosten.entities.MonetaryAmount
import de.cramer.nebenkosten.entities.SplitAlgorithmType
import de.cramer.nebenkosten.exceptions.BadRequestException
import de.cramer.nebenkosten.extensions.set
import de.cramer.nebenkosten.forms.InvoiceForm
import de.cramer.nebenkosten.forms.InvoiceType
import de.cramer.nebenkosten.services.ContractService
import de.cramer.nebenkosten.services.InvoiceService
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
import java.time.LocalDate
import java.time.Year
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

@Controller
@RequestMapping("invoices")
class InvoiceController(
    private val log: Logger,
    private val invoiceService: InvoiceService,
    private val contractService: ContractService,
) {

    @GetMapping
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
        @RequestParam(name = "templateId") templateId: Long?,
        year: Year,
        model: Model,
    ): String {
        val invoice = templateId?.let { invoiceService.getInvoice(it) }
        val firstDayOfYear = year.atDay(1)
        model["invoiceTypes"] = de.cramer.nebenkosten.entities.InvoiceType.entries
        model["splitAlgorithmTypes"] = SplitAlgorithmType.entries
        model["contracts"] = contractService.getContractsByPeriod(LocalDatePeriod.ofYear(year))
        model["defaultStart"] = firstDayOfYear
        model["defaultEnd"] = firstDayOfYear.with(TemporalAdjusters.lastDayOfYear())
        model["defaultDescription"] = invoice?.description ?: ""
        model["defaultPrice"] = invoice?.price ?: MonetaryAmount()
        model["defaultType"] = invoice?.type
        model["selectedSplitAlgorithmType"] = (invoice as? GeneralInvoice)?.splitAlgorithm?.type ?: ""
        model["selectedContract"] = (invoice as? ContractInvoice)?.contract ?: ""
        model["defaultOrder"] = invoice?.order
        return "invoice"
    }

    @PostMapping("create")
    fun createInvoice(
        @SessionAttribute
        @RequestParam("description")
        description: String,
        @RequestParam("price") price: Long,
        @RequestParam("type") type: InvoiceType,
        @RequestParam("splitAlgorithm", required = false) splitAlgorithmType: SplitAlgorithmType?,
        @RequestParam("contract", required = false) contract: Long?,
        @RequestParam("order") order: Int,
        @RequestParam("start") start: LocalDate,
        @RequestParam("end", required = false) end: LocalDate?,
        redirectAttributes: RedirectAttributes,
    ): String = try {
        InvoiceForm(description, price, type, splitAlgorithmType, contract, order, start, end).apply {
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
        @Suppress("UnusedParameter")
        @RequestParam("error", defaultValue = "[]")
        error: List<String>,
        year: Year,
        model: Model,
    ): String {
        val invoice = invoiceService.getInvoice(id)
        model["invoice"] = invoice
        model["invoiceTypes"] = de.cramer.nebenkosten.entities.InvoiceType.entries
        model["splitAlgorithmTypes"] = SplitAlgorithmType.entries
        model["contracts"] = contractService.getContractsByPeriod(LocalDatePeriod.ofYear(year))
        model["selectedSplitAlgorithmType"] = (invoice as? GeneralInvoice)?.splitAlgorithm?.type ?: ""
        model["selectedContract"] = (invoice as? ContractInvoice)?.contract ?: ""
        model["askBeforeSave"] = ChronoUnit.MONTHS.between(invoice.period.end, LocalDate.now()) >= 2
        return "invoice"
    }

    @PostMapping("edit/{id}")
    fun editInvoice(
        @PathVariable("id") id: Long,
        @RequestParam("description") description: String,
        @RequestParam("price") price: Long,
        @RequestParam("type") type: InvoiceType,
        @RequestParam("splitAlgorithm", required = false) splitAlgorithmType: SplitAlgorithmType?,
        @RequestParam("contract", required = false) contract: Long?,
        @RequestParam("order") order: Int,
        @RequestParam("start") start: LocalDate,
        @RequestParam("end", required = false) end: LocalDate?,
        redirectAttributes: RedirectAttributes,
    ): String = try {
        InvoiceForm(description, price, type, splitAlgorithmType, contract, order, start, end).apply {
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
