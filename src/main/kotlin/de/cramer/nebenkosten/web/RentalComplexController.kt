package de.cramer.nebenkosten.web

import de.cramer.nebenkosten.entities.User
import de.cramer.nebenkosten.extensions.set
import de.cramer.nebenkosten.forms.RentalComplexForm
import de.cramer.nebenkosten.services.RentalComplexService
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
@RequestMapping("rentalComplexes")
class RentalComplexController(
    private val log: Logger,
    private val rentalComplexService: RentalComplexService,
) {

    @GetMapping
    fun getRentalComplexes(
        @RequestParam("success") success: String?,
        @RequestParam("error") error: String?,
        @RequestParam("errorMessage") errorMessage: String?,
        user: User,
        model: Model,
    ): String {
        model["rentalComplexes"] = rentalComplexService.getRentalComplexes(user)
        model["success"] = success
        model["error"] = error
        model["errorMessage"] = errorMessage
        return "rentalComplexes"
    }

    @GetMapping("create")
    @Suppress("FunctionOnlyReturningConstant")
    fun createRentalComplex(): String {
        return "rentalComplex"
    }

    @PostMapping("create")
    fun createRentalComplex(
        @RequestParam("name") name: String,
        user: User,
        redirectAttributes: RedirectAttributes,
    ): String = try {
        rentalComplexService.createRentalComplex(RentalComplexForm(name), user)
        redirectAttributes["success"] = "create"
        "redirect:/rentalComplexes"
    } catch (e: Exception) {
        log.error(e.message, e)
        redirectAttributes["error"] = "create"
        redirectAttributes["errorMessage"] = e.message ?: ""
        "redirect:/rentalComplexes"
    }

    @GetMapping("show/{id}")
    fun getRentalComplex(
        @PathVariable("id") id: Long,
        model: Model,
    ): String {
        model["rentalComplex"] = rentalComplexService.getRentalComplex(id)
        return "rentalComplex"
    }

    @PostMapping("edit/{id}")
    fun editRentalComplex(
        @PathVariable("id") id: Long,
        @RequestParam("name") name: String,
        redirectAttributes: RedirectAttributes,
    ): String = try {
        rentalComplexService.editRentalComplex(id, RentalComplexForm(name))
        redirectAttributes["success"] = "edit"
        "redirect:/rentalComplexes"
    } catch (e: Exception) {
        log.error(e.message, e)
        redirectAttributes["error"] = "edit"
        redirectAttributes["errorMessage"] = e.message ?: ""
        "redirect:/rentalComplexes/show/$id"
    }

    @PostMapping("delete/{id}")
    fun alterRentalComplex(
        @PathVariable("id") id: Long,
        redirectAttributes: RedirectAttributes,
    ): String = try {
        rentalComplexService.deleteRentalComplex(id)
        redirectAttributes["success"] = "delete"
        "redirect:/rentalComplexes"
    } catch (e: Exception) {
        log.error(e.message, e)
        redirectAttributes["error"] = "delete"
        redirectAttributes["errorMessage"] = e.message ?: ""
        "redirect:/rentalComplexes/show/$id"
    }
}
