@file:Suppress("MagicNumber")

package de.cramer.nebenkosten.config

import de.cramer.nebenkosten.entities.Contract
import de.cramer.nebenkosten.entities.FormOfAddress
import de.cramer.nebenkosten.entities.Gender
import de.cramer.nebenkosten.entities.LocalDatePeriod
import de.cramer.nebenkosten.entities.SplitAlgorithmType
import de.cramer.nebenkosten.entities.User
import de.cramer.nebenkosten.forms.ContractForm
import de.cramer.nebenkosten.forms.FlatForm
import de.cramer.nebenkosten.forms.InvoiceForm
import de.cramer.nebenkosten.forms.InvoiceType
import de.cramer.nebenkosten.forms.LandlordForm
import de.cramer.nebenkosten.forms.RentalComplexForm
import de.cramer.nebenkosten.forms.TenantForm
import de.cramer.nebenkosten.services.ContractService
import de.cramer.nebenkosten.services.FlatService
import de.cramer.nebenkosten.services.InvoiceService
import de.cramer.nebenkosten.services.LandlordService
import de.cramer.nebenkosten.services.RentalComplexService
import de.cramer.nebenkosten.services.TenantService
import de.cramer.nebenkosten.services.UserService
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.math.BigInteger
import java.time.LocalDate
import java.time.Month
import java.time.Year
import java.time.YearMonth
import kotlin.math.roundToLong

@Component
@Profile("development")
class DevelopmentConfiguration {

    @Bean
    fun initializer(
        userService: UserService,
        rentalComplexService: RentalComplexService,
        landlordService: LandlordService,
        flatService: FlatService,
        tenantService: TenantService,
        contractService: ContractService,
        invoiceService: InvoiceService,
        passwordEncoder: PasswordEncoder,
    ) = CommandLineRunner {
        val rentalComplex = rentalComplexService.createRentalComplex(RentalComplexForm("Kastanienallee 39"))
        val secondRentalComplex = rentalComplexService.createRentalComplex(RentalComplexForm("Hallesches Ufer 31"))

        userService.saveUser(User("test", passwordEncoder.encode("user123"), mutableListOf(rentalComplex, secondRentalComplex)))

        landlordService.createLandlord(LandlordForm("Torsten", "Schweitzer", "Hans-Grade-Allee", 86, "24875", "Havetoftloit", null, "DE81 5001 0517 8228 8538 63", Year.of(2014), Year.of(2016)), rentalComplex)
        landlordService.createLandlord(LandlordForm("Monika", "Bader", "Landsberger Allee", 79, "80076", "München", null, "DE84 7402 0100 6161 4961 81", Year.of(2017)), rentalComplex)

        val erdgeschoss = flatService.createFlat(FlatForm("Erdgeschoss", 75), rentalComplex)
        val og1 = flatService.createFlat(FlatForm("1. Obergeschoss", 75, 1), rentalComplex)
        val dachgeschoss = flatService.createFlat(FlatForm("Dachgeschoss", 60, 2), rentalComplex)

        val tenant1 = tenantService.createTenant(TenantForm("Uta", "Krüger", "Bayreuther Straße", 76, "67659", "Kaiserslautern", gender = Gender.MALE, formOfAddress = FormOfAddress.INFORMAL, hidden = false), rentalComplex)
        val tenant2 = tenantService.createTenant(TenantForm("Max", "Propst", "Neuer Jungfernstieg", 96, "84126", "Dingolfing", gender = Gender.MALE, formOfAddress = FormOfAddress.FORMAL, hidden = true), rentalComplex)
        val tenant3 = tenantService.createTenant(TenantForm("Sara", "Weiss", "Fugger Straße", 22, "14403", "Potsdam", gender = Gender.MALE, formOfAddress = FormOfAddress.INFORMAL, hidden = false), rentalComplex)
        val tenant4 = tenantService.createTenant(TenantForm("Phillipp", "Wurfel", "Waßmannsdorfer Chaussee", 1, "21035", "Hamburg", gender = Gender.FEMALE, formOfAddress = FormOfAddress.INFORMAL, hidden = false), rentalComplex)

        val tenant1Erdgeschoss = contractService.createContract(ContractForm(erdgeschoss.id, tenant1.id, 2, LocalDate.of(2018, Month.FEBRUARY, 1)))
        val tenant2Og1 = contractService.createContract(ContractForm(og1.id, tenant2.id, 2, LocalDate.of(2010, Month.JANUARY, 1), YearMonth.of(2019, Month.MAY).atEndOfMonth()))
        val tenant3Og1 = contractService.createContract(ContractForm(og1.id, tenant3.id, 1, LocalDate.of(2019, Month.DECEMBER, 1)))
        val tenant4Dachgeschoss = contractService.createContract(ContractForm(dachgeschoss.id, tenant4.id, 1, LocalDate.of(2017, Month.NOVEMBER, 1)))

        val year = Year.of(2019)
        invoiceService.createInvoice(generalInvoice("Wasser", 223.euros() + 46.cents(), SplitAlgorithmType.ByPersons, year, 1), rentalComplex)
        invoiceService.createInvoice(generalInvoice("Abwasser", 532.euros() + 17.cents(), SplitAlgorithmType.ByPersons, year, 2), rentalComplex)
        invoiceService.createInvoice(generalInvoice("Grundsteuer", 458.euros() + 76.cents(), SplitAlgorithmType.ByArea, year, 4), rentalComplex)
        invoiceService.createInvoice(generalInvoice("Haftpflichtversicherung", 75.euros() + 72.cents(), SplitAlgorithmType.ByArea, year, 5), rentalComplex)
        invoiceService.createInvoice(generalInvoice("Wohngebäudeversicherung", 699.euros() + 54.cents(), SplitAlgorithmType.ByArea, year, 6), rentalComplex)
        invoiceService.createInvoice(generalInvoice("Wartung Feuerlöscher", 25.euros(), SplitAlgorithmType.ByArea, year, 7), rentalComplex)
        invoiceService.createInvoice(contractInvoice("Heizung + Vorauszahlung", (-415).euros() - 66.cents(), tenant1Erdgeschoss, year, 0), rentalComplex)
        invoiceService.createInvoice(contractInvoice("Heizung + Vorauszahlung", (-249).euros() - 90.cents(), tenant2Og1, year, 0), rentalComplex)
        invoiceService.createInvoice(contractInvoice("Heizung + Vorauszahlung", 129.euros() + 44.cents(), tenant3Og1, year, 0), rentalComplex)
        invoiceService.createInvoice(contractInvoice("Heizung + Vorauszahlung", 218.euros() + 34.cents(), tenant4Dachgeschoss, year, 0), rentalComplex)
        invoiceService.createInvoice(contractInvoice("Müll", 134.euros() + 91.cents(), tenant1Erdgeschoss, year, 3), rentalComplex)
        invoiceService.createInvoice(contractInvoice("Müll", 65.euros() + 77.cents(), tenant2Og1, year, 3), rentalComplex)
        invoiceService.createInvoice(contractInvoice("Müll", 0.euros() + 0.cents(), tenant3Og1, year, 3), rentalComplex)
        invoiceService.createInvoice(contractInvoice("Müll", 73.euros() + 33.cents(), tenant4Dachgeschoss, year, 3), rentalComplex)
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

fun contractInvoice(description: String, priceInCent: Long, contract: Contract, year: Year, order: Int = 0): InvoiceForm {
    val period = LocalDatePeriod.ofYear(year).intersect(contract.period)
    return InvoiceForm(description, priceInCent, InvoiceType.Contract, null, contract.id, order, period.start, period.end)
}
