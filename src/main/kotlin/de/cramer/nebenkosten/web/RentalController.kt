package de.cramer.nebenkosten.web

import de.cramer.nebenkosten.entities.LocalDatePeriod
import de.cramer.nebenkosten.exceptions.BadRequestException
import de.cramer.nebenkosten.forms.RentalForm
import de.cramer.nebenkosten.services.FlatService
import de.cramer.nebenkosten.services.RentalService
import de.cramer.nebenkosten.services.TenantService
import org.slf4j.Logger
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.time.LocalDate
import java.time.Year

@Controller
@RequestMapping("rentals")
class RentalController(
    private val log: Logger,
    private val rentalService: RentalService,
    private val flatService: FlatService,
    private val tenantService: TenantService
) {

    @GetMapping("")
    fun getRentals(
        year: Year,
        model: Model
    ): String {
        model["rentals"] = rentalService.getRentalsByPeriod(LocalDatePeriod.ofYear(year))
        return "rentals"
    }

    @GetMapping("create")
    fun createRental(): String {
        return "rental"
    }

    @PostMapping("create")
    fun createRental(
        @RequestParam("flat") flatName: String,
        @RequestParam("tenant") tenantId: Long,
        @RequestParam("persons") persons: Int,
        @RequestParam("start") start: LocalDate,
        @RequestParam("end", required = false) end: LocalDate?,
        redirectAttributes: RedirectAttributes
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
        model: Model
    ): String {
        model["rental"] = rentalService.getRental(id)
        model["flats"] = flatService.getFlats()
        model["tenants"]= tenantService.getTenants(false)
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
        redirectAttributes: RedirectAttributes
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
        redirectAttributes: RedirectAttributes
    ): String = try {
        rentalService.deleteRental(id)
        "redirect:/rentals"
    } catch (e: Exception) {
        log.error(e.message, e)
        redirectAttributes["error"] = "delete"
        redirectAttributes["errorMessage"] = e.message ?: ""
        "redirect:/rentals/show/$id"
    }
}
