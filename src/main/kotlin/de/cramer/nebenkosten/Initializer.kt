package de.cramer.nebenkosten

import java.math.BigInteger
import java.time.LocalDate
import java.time.Month
import java.time.Year
import java.time.YearMonth
import kotlin.math.roundToLong
import de.cramer.nebenkosten.entities.FormOfAddress
import de.cramer.nebenkosten.entities.Gender
import de.cramer.nebenkosten.entities.LocalDatePeriod
import de.cramer.nebenkosten.entities.Rental
import de.cramer.nebenkosten.entities.SplitAlgorithmType
import de.cramer.nebenkosten.forms.FlatForm
import de.cramer.nebenkosten.forms.InvoiceForm
import de.cramer.nebenkosten.forms.InvoiceType
import de.cramer.nebenkosten.forms.RentalForm
import de.cramer.nebenkosten.forms.TenantForm
import de.cramer.nebenkosten.services.BillingService
import de.cramer.nebenkosten.services.FlatService
import de.cramer.nebenkosten.services.InvoiceService
import de.cramer.nebenkosten.services.RentalService
import de.cramer.nebenkosten.services.TenantService
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("development")
class Initializer {

    @Bean
    fun runner(
        flatService: FlatService,
        tenantService: TenantService,
        rentalService: RentalService,
        invoiceService: InvoiceService,
        billingService: BillingService,
    ) = CommandLineRunner {
        val erdgeschoss = flatService.createFlat(FlatForm("Erdgeschoss", 75))
        val og1 = flatService.createFlat(FlatForm("1. Obergeschoss", 75, 1))
        val dachgeschoss = flatService.createFlat(FlatForm("Dachgeschoss", 60, 2))

        val tenant1 = tenantService.createTenant(TenantForm("Uta", "Krüger", "Bayreuther Straße", 76, "67659", "Kaiserslautern", gender = Gender.MALE, formOfAddress = FormOfAddress.INFORMAL, hidden = false))
        val tenant2 = tenantService.createTenant(TenantForm("Max", "Propst", "Neuer Jungfernstieg", 96, "84126", "Dingolfing", gender = Gender.MALE, formOfAddress = FormOfAddress.FORMAL, hidden = true))
        val tenant3 = tenantService.createTenant(TenantForm("Sara", "Weiss", "Fugger Straße", 22, "14403", "Potsdam", gender = Gender.MALE, formOfAddress = FormOfAddress.INFORMAL, hidden = false))
        val tenant4 = tenantService.createTenant(TenantForm("Phillipp", "Wurfel", "Waßmannsdorfer Chaussee", 1, "21035", "Hamburg", gender = Gender.FEMALE, formOfAddress = FormOfAddress.INFORMAL, hidden = false))

        val tenant1Erdgeschoss = rentalService.createRental(RentalForm(erdgeschoss.name, tenant1.id, 2, LocalDate.of(2018, Month.FEBRUARY, 1)))
        val tenant2Og1 = rentalService.createRental(RentalForm(og1.name, tenant2.id, 2, LocalDate.of(2010, Month.JANUARY, 1), YearMonth.of(2019, Month.MAY).atEndOfMonth()))
        val tenant3Og1 = rentalService.createRental(RentalForm(og1.name, tenant3.id, 1, LocalDate.of(2019, Month.DECEMBER, 1)))
        val tenant4Dachgeschoss = rentalService.createRental(RentalForm(dachgeschoss.name, tenant4.id, 1, LocalDate.of(2017, Month.NOVEMBER, 1)))

        val year = Year.of(2019)
        invoiceService.createInvoice(generalInvoice("Wasser", 223.euros() + 46.cents(), SplitAlgorithmType.ByPersons, year, 1))
        invoiceService.createInvoice(generalInvoice("Abwasser", 532.euros() + 17.cents(), SplitAlgorithmType.ByPersons, year, 2))
        invoiceService.createInvoice(generalInvoice("Grundsteuer", 458.euros() + 76.cents(), SplitAlgorithmType.ByArea, year, 4))
        invoiceService.createInvoice(generalInvoice("Haftpflichtversicherung", 75.euros() + 72.cents(), SplitAlgorithmType.ByArea, year, 5))
        invoiceService.createInvoice(generalInvoice("Wohngebäudeversicherung", 699.euros() + 54.cents(), SplitAlgorithmType.ByArea, year, 6))
        invoiceService.createInvoice(generalInvoice("Wartung Feuerlöscher", 25.euros(), SplitAlgorithmType.ByArea, year, 7))
        invoiceService.createInvoice(rentalInvoice("Heizung + Vorauszahlung", (-415).euros() - 66.cents(), tenant1Erdgeschoss, year, 0))
        invoiceService.createInvoice(rentalInvoice("Heizung + Vorauszahlung", (-249).euros() - 90.cents(), tenant2Og1, year, 0))
        invoiceService.createInvoice(rentalInvoice("Heizung + Vorauszahlung", 129.euros() + 44.cents(), tenant3Og1, year, 0))
        invoiceService.createInvoice(rentalInvoice("Heizung + Vorauszahlung", 218.euros() + 34.cents(), tenant4Dachgeschoss, year, 0))
        invoiceService.createInvoice(rentalInvoice("Müll", 134.euros() + 91.cents(), tenant1Erdgeschoss, year, 3))
        invoiceService.createInvoice(rentalInvoice("Müll", 65.euros() + 77.cents(), tenant2Og1, year, 3))
        invoiceService.createInvoice(rentalInvoice("Müll", 0.euros() + 0.cents(), tenant3Og1, year, 3))
        invoiceService.createInvoice(rentalInvoice("Müll", 73.euros() + 33.cents(), tenant4Dachgeschoss, year, 3))
    }
}

fun Number.cents(): Long = when (this) {
    is Byte -> this.toLong()
    is Short -> this.toLong()
    is Int -> this.toLong()
    is Long -> this
    is Float -> this.roundToLong()
    is Double -> this.roundToLong()
    is BigInteger -> this.toLong()
    else -> this.toDouble().roundToLong()
}

fun Number.euros(): Long = (this.toDouble() * 100).cents()

fun generalInvoice(description: String, priceInCent: Long, splitAlgorithmType: SplitAlgorithmType, year: Year, order: Int = 0): InvoiceForm {
    val period = LocalDatePeriod.ofYear(year)
    return InvoiceForm(description, priceInCent, InvoiceType.General, splitAlgorithmType, null, order, period.start, period.end)
}

fun rentalInvoice(description: String, priceInCent: Long, rental: Rental, year: Year, order: Int = 0): InvoiceForm {
    val period = LocalDatePeriod.ofYear(year).intersect(rental.period)
    return InvoiceForm(description, priceInCent, InvoiceType.Rental, null, rental.id, order, period.start, period.end)
}
