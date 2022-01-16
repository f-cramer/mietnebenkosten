package de.cramer.nebenkosten.web

import java.time.LocalDate
import java.time.Year
import de.cramer.nebenkosten.entities.Flat
import de.cramer.nebenkosten.entities.LocalDatePeriod
import de.cramer.nebenkosten.entities.Rental
import de.cramer.nebenkosten.exceptions.BadRequestException
import de.cramer.nebenkosten.extensions.set
import de.cramer.nebenkosten.forms.RentalForm
import de.cramer.nebenkosten.services.FlatService
import de.cramer.nebenkosten.services.RentalService
import de.cramer.nebenkosten.services.TenantService
import org.slf4j.Logger
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("rentals")
class RentalController(
    private val log: Logger,
    private val rentalService: RentalService,
    private val flatService: FlatService,
    private val tenantService: TenantService,
) {

    @GetMapping
    fun getRentals(
        year: Year,
        model: Model,
    ): String {
        val flats = flatService.getFlats().toMutableList()
        val rentalsByFlat = rentalService.getRentalsByPeriod(LocalDatePeriod.ofYear(year))
            .groupBy { it.flat }
            .asSequence()
            .map { RentalsByFlat(it.key, it.value) }
            .onEach { flats -= it.flat }
            .toList().asSequence() // evaluate onEach eagerly
            .plus(
                flats.map { RentalsByFlat(it, emptyList()) }
            )
            .sorted()
            .toList()
        model["rentalsByFlat"] = rentalsByFlat
        return "rentals"
    }

    @GetMapping("create")
    fun createRental(
        @RequestParam(name = "flat", required = false) flatName: String?,
        model: Model,
    ): String {
        val flats = flatService.getFlats()
        val flat = if (flatName != null) flats.firstOrNull { it.name == flatName } else null
        if (flat != null) {
            model["selectedFlat"] = flat
        }
        model["flats"] = flats
        model["tenants"] = tenantService.getTenants(false)
        return "rental"
    }

    @PostMapping("create")
    fun createRental(
        @RequestParam("flat") flatName: String,
        @RequestParam("tenant") tenantId: Long,
        @RequestParam("persons") persons: Int,
        @RequestParam("start") start: LocalDate,
        @RequestParam("end", required = false) end: LocalDate?,
        redirectAttributes: RedirectAttributes,
    ): String = try {
        RentalForm(flatName, tenantId, persons, start, end).apply {
            validate()
            rentalService.createRental(this)
        }
        "redirect:/rentals"
    } catch (e: Exception) {
        log.error(e.message, e)
        redirectAttributes["error"] = "create"
        redirectAttributes["errorMessage"] = e.message ?: ""
        "redirect:/rentals"
    }

    @GetMapping("show/{id}")
    fun getRental(
        @PathVariable("id") id: Long,
        model: Model,
    ): String {
        model["rental"] = rentalService.getRental(id)
        model["flats"] = flatService.getFlats()
        model["tenants"] = tenantService.getTenants(false)
        return "rental"
    }

    @PostMapping("edit/{id}")
    fun editRental(
        @PathVariable("id") id: Long,
        @RequestParam("flat") flatName: String,
        @RequestParam("tenant") tenantId: Long,
        @RequestParam("persons") persons: Int,
        @RequestParam("start") start: LocalDate,
        @RequestParam("end", required = false) end: LocalDate?,
        redirectAttributes: RedirectAttributes,
    ): String = try {
        RentalForm(flatName, tenantId, persons, start, end).apply {
            validate()
            rentalService.editRental(id, this)
        }
        "redirect:/rentals"
    } catch (e: BadRequestException) {
        log.debug(e.message, e)
        redirectAttributes["error"] = "badRequest"
        redirectAttributes["errorMessage"] = e.message ?: ""
        "redirect:/rentals/show/$id"
    } catch (e: Exception) {
        log.error(e.message, e)
        redirectAttributes["error"] = "edit"
        redirectAttributes["errorMessage"] = e.message ?: ""
        "redirect:/rentals/show/$id"
    }

    @PostMapping("delete/{id}")
    fun alterRental(
        @PathVariable("id") id: Long,
        redirectAttributes: RedirectAttributes,
    ): String = try {
        rentalService.deleteRental(id)
        "redirect:/rentals"
    } catch (e: Exception) {
        log.error(e.message, e)
        redirectAttributes["error"] = "delete"
        redirectAttributes["errorMessage"] = e.message ?: ""
        "redirect:/rentals/show/$id"
    }

    private data class RentalsByFlat(
        val flat: Flat,
        val rentals: List<Rental>,
    ) : Comparable<RentalsByFlat> {

        override fun compareTo(other: RentalsByFlat) =
            COMPARATOR.compare(this, other)

        companion object {

            private val COMPARATOR = compareBy<RentalsByFlat> { it.flat }
        }
    }
}
