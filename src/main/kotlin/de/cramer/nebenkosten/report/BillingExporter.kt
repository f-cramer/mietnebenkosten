package de.cramer.nebenkosten.report

import com.fasterxml.jackson.databind.ObjectMapper
import de.cramer.nebenkosten.entities.Billing
import de.cramer.nebenkosten.entities.BillingEntry
import de.cramer.nebenkosten.entities.GeneralInvoice
import de.cramer.nebenkosten.entities.RentalInvoice
import net.sf.jasperreports.engine.JRParameter.REPORT_LOCALE
import net.sf.jasperreports.engine.JRParameter.REPORT_RESOURCE_BUNDLE
import net.sf.jasperreports.engine.JasperCompileManager
import net.sf.jasperreports.engine.JasperFillManager
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.JasperReport
import net.sf.jasperreports.engine.data.JsonDataSource
import net.sf.jasperreports.engine.export.JRPdfExporter
import net.sf.jasperreports.engine.query.JsonQueryExecuterFactory.*
import net.sf.jasperreports.export.SimpleExporterInput
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput
import org.springframework.context.MessageSource
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import javax.annotation.PostConstruct

@Service
class BillingExporter(
    private val objectMapper: ObjectMapper,
    private val resourceLoader: ResourceLoader,
    private val messageSource: MessageSource
) {

    private val report: JasperReport by lazy {
        resourceLoader.getResource("classpath:/reports/billings-json.jrxml").inputStream.use {
            JasperCompileManager.compileReport(it)
        }
    }

    @PostConstruct
    fun initialize() {
        report.compilerClass // explicitly compiling report
    }

    fun export(
        billing: Billing,
        locale: Locale
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

    protected fun createJasperPrint(source: String, parameters: Map<String, Any>): JasperPrint {
        val dataSource = JsonDataSource(source.byteInputStream(Charsets.UTF_8))
        dataSource.datePattern = parameters[JSON_DATE_PATTERN]?.toString() ?: "yyyy-MM-dd"
        dataSource.setLocale(parameters[JSON_LOCALE]?.toString() ?: Locale.US.toString())
        dataSource.numberPattern = parameters[JSON_NUMBER_PATTERN]?.toString() ?: ""
        return JasperFillManager.fillReport(report, parameters, dataSource)
    }

    private fun addDefaultParameters(parameters: MutableMap<String, Any>, locale: Locale) {
        parameters.putIfAbsent(REPORT_LOCALE, locale)
        parameters.putIfAbsent(JSON_LOCALE, Locale.US)
        parameters.putIfAbsent(JSON_DATE_PATTERN, "yyyy-MM-dd HH:mm:ss") //$NON-NLS-1$
        parameters.putIfAbsent(REPORT_RESOURCE_BUNDLE, SpringResourceBundle(locale))
    }

    private fun Billing.toReport(): BillingReport = BillingReport(
        sender = ReportAddress(
            name = "Monika Bader",
            street1 = "Landsberger Allee 79",
            street2 = "",
            city = "80076 München",
            country = ""
        ),
        receiver = ReportAddress(
            name = "${tenant.firstName} ${tenant.lastName}",
            street1 = "${tenant.address.street} ${tenant.address.houseNumber}",
            street2 = "",
            city = "${tenant.address.zipCode} ${tenant.address.city}",
            country = tenant.address.country ?: ""
        ),
        note = ReportNote(
            title = "Abrechnung Nebenkosten",
            text = getNote()
        ),
        billingSum = ReportBillingSum(
            totalPrice = total.amount
        ),
        billings = entries
            .map { it.toReportBilling() }
    )

    private fun Billing.getNote(): String = """
        Hallo ${tenant.firstName},
        hiermit erhälst du deine Mietnebenkostenabrechnung für den Rechnungszeitraum ${getPeriodFormatted()}.
        Ich bitte um Überweisung einer etwaigen Nachzahlung an IBAN: DE84 7402 0100 6161 4961 81.
    """.trimIndent()

    private fun Billing.getPeriodFormatted(): String = if (period.end != null) {
        "vom ${getStartFormatted()} bis zum ${getEndFormatted()}"
    } else {
        "ab dem ${getStartFormatted()}"
    }

    private fun Billing.getStartFormatted(): String = period.start.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

    private fun Billing.getEndFormatted(): String = period.end?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) ?: ""

    private fun BillingEntry.toReportBilling(): ReportBilling {
        return ReportBilling(
            name = invoice.description,
            splitUnit = when (invoice) {
                is GeneralInvoice -> invoice.splitAlgorithm.unit
                is RentalInvoice -> ""
            },
            totalValue = totalValue,
            totalPrice = totalValue.let { invoice.price.amount },
            partValue = proportionalValue,
            partPrice = proportionalPrice.amount
        )
    }

    private inner class SpringResourceBundle(
        private val currentLocale: Locale
    ): ResourceBundle() {

        override fun handleGetObject(key: String): String = messageSource.getMessage(key, null, currentLocale)

        override fun getKeys(): Enumeration<String> = Collections.emptyEnumeration()
    }
}
