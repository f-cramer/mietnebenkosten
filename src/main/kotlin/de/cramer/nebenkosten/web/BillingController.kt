package de.cramer.nebenkosten.web

import de.cramer.nebenkosten.entities.Billing
import de.cramer.nebenkosten.exceptions.NoLandlordFoundException
import de.cramer.nebenkosten.extensions.set
import de.cramer.nebenkosten.reports.BillingExporter
import de.cramer.nebenkosten.services.BillingService
import org.slf4j.Logger
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.text.NumberFormat
import java.time.Year
import java.util.Locale

@Controller
@RequestMapping("billings")
class BillingController(
    private val billingService: BillingService,
    private val billingExporter: BillingExporter,
    private val log: Logger,
) {

    @GetMapping
    fun getBilling(
        year: Year,
        locale: Locale,
        model: Model,
    ): String {
        model["valueFormat"] = NumberFormat.getNumberInstance(LocaleContextHolder.getLocale()).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 2
        }
        try {
            val billings = billingService.createBillings(year, locale, true)
            model["billings"] = billings
        } catch (e: Exception) {
            if (e is NoLandlordFoundException) {
                model["warning"] = e.message
            } else {
                log.error(e.message, e)
                model["error"] = e.message
            }
            model["billings"] = emptyList<Billing>()
        }
        return "billing"
    }

    @GetMapping("export")
    fun downloadPdf(
        @RequestParam(name = "tenant") tenantId: Long,
        year: Year,
        locale: Locale,
    ): ResponseEntity<Resource> {
        val (billing, resource) = getResource(year, tenantId, locale)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_PDF
        headers.setContentDispositionFormData("attachment", "${billing.tenant.name} $year.pdf")

        return ResponseEntity.status(HttpStatus.OK)
            .headers(headers)
            .body(resource)
    }

    @GetMapping("show")
    fun showPdf(
        @RequestParam(name = "tenant") tenantId: Long,
        year: Year,
        locale: Locale,
    ): ResponseEntity<Resource> {
        val (_, resource) = getResource(year, tenantId, locale)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_PDF

        return ResponseEntity.status(HttpStatus.OK)
            .headers(headers)
            .body(resource)
    }

    private fun getResource(year: Year, tenantId: Long, locale: Locale): Pair<Billing, ByteArrayResource> {
        val billing = billingService.createBillings(year, locale, true)
            .first { it.tenant.id == tenantId }
        val pdfData = billingExporter.export(billing, locale)
        val resource = ByteArrayResource(pdfData)
        return Pair(billing, resource)
    }
}
