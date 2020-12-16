package de.cramer.nebenkosten

import de.cramer.nebenkosten.entities.*
import de.cramer.nebenkosten.forms.BillForm
import de.cramer.nebenkosten.forms.FlatForm
import de.cramer.nebenkosten.forms.RentalForm
import de.cramer.nebenkosten.forms.TenantForm
import de.cramer.nebenkosten.services.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InjectionPoint
import org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month
import java.time.Year
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters.firstDayOfYear
import java.time.temporal.TemporalAdjusters.lastDayOfYear
import kotlin.math.roundToLong

@SpringBootApplication
class MietnebenkostenApplication {

    @Bean
    fun runner(
        flatService: FlatService,
        tenantService: TenantService,
        rentalService: RentalService,
        billService: BillService,
        billingService: BillingService
    ) = CommandLineRunner {
        val erdgeschoss = flatService.createFlat(FlatForm("Erdgeschoss", 75))
        val og1 = flatService.createFlat(FlatForm("1. Obergeschoss", 75, 1))
        val dachgeschoss = flatService.createFlat(FlatForm("Dachgeschoss", 60, 2))

        val tenant1 = tenantService.createTenant(TenantForm("Uta", "Krüger", "Bayreuther Straße", 76, "67659", "Kaiserslautern", hidden = false))
        val tenant2 = tenantService.createTenant(TenantForm("Max", "Propst", "Neuer Jungfernstieg", 96, "84126", "Dingolfing", hidden = true))
        val tenant3 = tenantService.createTenant(TenantForm("Sara", "Weiss", "Fugger Straße", 22, "14403", "Potsdam", hidden = false))
        val tenant4 = tenantService.createTenant(TenantForm("Phillipp", "Wurfel", "Waßmannsdorfer Chaussee", 1, "21035", "Hamburg", hidden = false))

        rentalService.createRental(RentalForm(erdgeschoss.name, tenant1.id, 2, LocalDate.of(2018, Month.FEBRUARY, 1)))
        rentalService.createRental(RentalForm(og1.name, tenant2.id, 2, LocalDate.of(2010, Month.JANUARY, 1), YearMonth.of(2019, Month.MAY).atEndOfMonth()))
        rentalService.createRental(RentalForm(og1.name, tenant3.id, 1, LocalDate.of(2019, Month.DECEMBER, 1)))
        rentalService.createRental(RentalForm(dachgeschoss.name, tenant4.id, 1, LocalDate.of(2017, Month.NOVEMBER, 1)))

        val year = Year.of(2019)
        val billings = flatService.getFlats().asSequence()
            .flatMap { billingService.createBilling(it, year).asSequence() }
            .sorted()
            .toList()
//        billings.forEach { println(it) }

        val someDayInYear = year.atDay(1)
        val bill = billService.createBill(BillForm("Wasser", 312.euros() + 51.cents(), someDayInYear.with(firstDayOfYear()), someDayInYear.with(lastDayOfYear())))

        val outputter = Outputter(bill, billings)
//        outputter.output(ByAreaSplitAlgorithm)
        outputter.output(ByPersonsSplitAlgorithm(SimplePersonFallback(0)))
//        outputter.output(ByPersonsSplitAlgorithm(SimplePersonFallback(1)))
//        outputter.output(LinearSplitAlgorithm)
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    fun logger(injectionPoint: InjectionPoint): Logger {
        val type = injectionPoint.member.declaringClass
        return LoggerFactory.getLogger(type)
    }
}

fun main(args: Array<String>) {
    runApplication<MietnebenkostenApplication>(*args)
}

fun Number.cents(): Long = when (this) {
    is Double -> this.roundToLong()
    is Float -> this.roundToLong()
    else -> this.toDouble().roundToLong()
}

fun Number.euros(): Long = (this.toDouble() * 100).cents()

fun MonetaryAmount.toEuros(): String = (BigDecimal(this.amount).setScale(2) / BigDecimal(100)).toString()

data class Outputter(
    private val bill: Bill,
    private val billings: Collection<Billing>
) {

    fun output(splitAlgorithm: SplitAlgorithm) {
        println(splitAlgorithm)
        val split = splitAlgorithm.split(bill, billings)
        split.forEach { println("${it.billing.rental.tenant} -> ${it.splittedAmount.toEuros()}") }
        println(split.sum())
        println()
    }
}

private fun List<BillSplit>.sum(): MonetaryAmount = this.asSequence()
    .map { it.splittedAmount }
    .reduceOrNull { acc, monetaryAmount -> acc + monetaryAmount } ?: MonetaryAmount(0)
