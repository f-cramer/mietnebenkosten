package de.cramer.nebenkosten.web

import de.cramer.nebenkosten.exceptions.BadRequestException
import de.cramer.nebenkosten.forms.RentalForm
import de.cramer.nebenkosten.services.RentalService
import org.slf4j.Logger
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.time.LocalDate

@Controller
@RequestMapping("rentals")
class RentalController(
    private val log: Logger,
    private val rentalService: RentalService
) {

    @GetMapping("")
    fun getRentals(
        @RequestParam(name = "includeClosed", defaultValue = "false") includeClosed: Boolean,
        model: Model
    ): String {
        model.addAttribute("includeClosed", includeClosed)
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
        redirectAttributes.addAttribute("error", "create")
        redirectAttributes.addAttribute("errorMessage", e.message)
        "redirect:/rentals"
    }

    @GetMapping("show/{id}")
    fun getRental(
        @PathVariable("id") id: Long,
        model: Model
    ): String {
        model.addAttribute("id", id)
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
        redirectAttributes.addAttribute("error", "badRequest")
        redirectAttributes.addFlashAttribute("errorMessage", e.message)
        "redirect:/rentals/show/$id"
    } catch (e: Exception) {
        log.error(e.message, e)
        redirectAttributes.addAttribute("error", "edit")
        redirectAttributes.addFlashAttribute("errorMessage", e.message)
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
        redirectAttributes.addAttribute("error", "delete")
        redirectAttributes.addFlashAttribute("errorMessage", e.message)
        "redirect:/rentals/show/$id"
    }
}
