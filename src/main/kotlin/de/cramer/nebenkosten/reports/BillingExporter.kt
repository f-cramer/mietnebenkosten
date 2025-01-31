package de.cramer.nebenkosten.reports

import com.fasterxml.jackson.databind.ObjectMapper
import de.cramer.nebenkosten.entities.Billing
import de.cramer.nebenkosten.entities.BillingEntry
import de.cramer.nebenkosten.entities.ContractInvoice
import de.cramer.nebenkosten.entities.FormOfAddress
import de.cramer.nebenkosten.entities.Gender
import de.cramer.nebenkosten.entities.GeneralInvoice
import de.cramer.nebenkosten.entities.LocalDatePeriod
import jakarta.annotation.PostConstruct
import net.sf.jasperreports.engine.JRParameter.REPORT_LOCALE
import net.sf.jasperreports.engine.JRParameter.REPORT_RESOURCE_BUNDLE
import net.sf.jasperreports.engine.JasperFillManager
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.data.JsonDataSource
import net.sf.jasperreports.engine.export.JRPdfExporter
import net.sf.jasperreports.engine.query.JsonQueryExecuterFactory.JSON_DATE_PATTERN
import net.sf.jasperreports.engine.query.JsonQueryExecuterFactory.JSON_LOCALE
import net.sf.jasperreports.engine.query.JsonQueryExecuterFactory.JSON_NUMBER_PATTERN
import net.sf.jasperreports.export.SimpleExporterInput
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput
import org.springframework.context.MessageSource
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Collections
import java.util.Enumeration
import java.util.Locale
import java.util.ResourceBundle

@Service
class BillingExporter(
    private val objectMapper: ObjectMapper,
    private val resourceLoader: ResourceLoader,
    private val messageSource: MessageSource,
) {

    private val report: Resource by lazy {
        resourceLoader.getResource("classpath:/de/cramer/nebenkosten/reports/billings-json.jasper")
    }

    @PostConstruct
    fun initialize() {
        require(report.exists()) { "report not found at ${report.uri}" }
    }

    fun export(
        billing: Billing,
        locale: Locale,
    ): ByteArray {
        val report = billing.toReport()
        val json = objectMapper.writeValueAsString(report)

        val parameters = mutableMapOf<String, Any>()
        addDefaultParameters(parameters, locale)

        val jasperPrint = createJasperPrint(json, parameters)

        val exporter = JRPdfExporter()
        exporter.setExporterInput(SimpleExporterInput.getInstance(listOf(jasperPrint)))
        return ByteArrayOutputStream().use {
            exporter.exporterOutput = SimpleOutputStreamExporterOutput(it)
            exporter.exportReport()
            it.toByteArray()
        }
    }

    private fun createJasperPrint(source: String, parameters: Map<String, Any>): JasperPrint {
        val dataSource = JsonDataSource(source.byteInputStream(Charsets.UTF_8))
        dataSource.datePattern = parameters[JSON_DATE_PATTERN]?.toString() ?: "yyyy-MM-dd"
        dataSource.setLocale(parameters[JSON_LOCALE]?.toString() ?: Locale.US.toString())
        dataSource.numberPattern = parameters[JSON_NUMBER_PATTERN]?.toString() ?: ""
        return report.inputStream.use {
            JasperFillManager.fillReport(it, parameters, dataSource)
        }
    }

    private fun addDefaultParameters(parameters: MutableMap<String, Any>, locale: Locale) {
        parameters.putIfAbsent(REPORT_LOCALE, locale)
        parameters.putIfAbsent(JSON_LOCALE, Locale.US)
        parameters.putIfAbsent(JSON_DATE_PATTERN, "yyyy-MM-dd HH:mm:ss")
        parameters.putIfAbsent(REPORT_RESOURCE_BUNDLE, SpringResourceBundle(locale))
    }

    private fun Billing.toReport(): BillingReport = BillingReport(
        sender = ReportAddress(
            name = "${landlord.firstName} ${landlord.lastName}",
            street1 = "${landlord.address.street} ${landlord.address.houseNumber}",
            street2 = "",
            city = "${landlord.address.zipCode} ${landlord.address.city}",
            country = landlord.address.country ?: "",
        ),
        receiver = ReportAddress(
            name = "${tenant.firstName} ${tenant.lastName}",
            street1 = "${tenant.address.street} ${tenant.address.houseNumber}",
            street2 = "",
            city = "${tenant.address.zipCode} ${tenant.address.city}",
            country = tenant.address.country ?: "",
        ),
        note = ReportNote(
            title = "Abrechnung Nebenkosten",
            text = getNote(),
        ),
        vacancyNote = getVacancyNote(),
        billingSum = ReportBillingSum(
            totalPrice = total.amount,
        ),
        billings = entries
            .map { it.toReportBilling() },
        generated = tenant.generated,
        year = periods.first().start.year,
    )

    private fun Billing.getNote(): String = when (tenant.formOfAddress) {
        FormOfAddress.INFORMAL ->
            """
                Hallo ${tenant.firstName},
                hiermit erhältst du deine Mietnebenkostenabrechnung für den Rechnungszeitraum ${getPeriodFormatted()}.
                Ich bitte um Überweisung einer etwaigen Nachzahlung an IBAN: ${landlord.iban}.
            """

        FormOfAddress.FORMAL ->
            """
                Guten Tag ${tenant.gender.address} ${tenant.lastName},
                hiermit erhalten Sie Ihre Mietnebenkostenabrechnung für den Rechnungszeitraum ${getPeriodFormatted()}.
                Ich bitte um Überweisung einer etwaigen Nachzahlung an IBAN: ${landlord.iban}.
            """
    }.trimIndent()

    private fun Billing.getVacancyNote(): String = """
        Zeiträume:
        ${
        periods.joinToString("\n") {
            val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
            val start = formatter.format(it.start)
            val end = it.end?.let { end -> formatter.format(end) } ?: ""
            "$start - $end"
        }
    }
    """.trimIndent()

    private fun Billing.getPeriodFormatted(): String = when (periods.size) {
        0 -> error("cannot format empty billing periods")
        1 -> periods.single().format()
        else -> buildString {
            val size = periods.size
            for ((index, period) in periods.withIndex()) {
                append(period.format())
                append(if (index < size - 1) ", " else " und ")
            }
        }
    }

    private fun LocalDatePeriod.format() = if (end != null) {
        "vom ${getStartFormatted()} bis zum ${getEndFormatted()}"
    } else {
        "ab dem ${getStartFormatted()}"
    }

    private fun LocalDatePeriod.getStartFormatted(): String = start.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

    private fun LocalDatePeriod.getEndFormatted(): String = end?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) ?: ""

    private val Gender.address: String
        get() = when (this) {
            Gender.FEMALE -> "Frau"
            Gender.MALE -> "Herr"
        }

    private fun BillingEntry.toReportBilling(): ReportBilling {
        return ReportBilling(
            name = invoice.description,
            splitUnit = when (invoice) {
                is GeneralInvoice -> invoice.splitAlgorithm.unit
                is ContractInvoice -> ""
                else -> error("unsupported invoice type $invoice")
            },
            totalValue = totalValue,
            totalPrice = totalValue.let { invoice.price.amount },
            partValue = proportionalValue,
            partPrice = proportionalPrice.amount,
        )
    }

    private inner class SpringResourceBundle(
        private val currentLocale: Locale,
    ) : ResourceBundle() {

        override fun handleGetObject(key: String): String = messageSource.getMessage(key, null, currentLocale)

        override fun getKeys(): Enumeration<String> = Collections.emptyEnumeration()
    }
}
