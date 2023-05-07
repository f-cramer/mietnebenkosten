package de.cramer.nebenkosten.web

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

@Controller
@RequestMapping("rentalComplexes")
class RentalComplexController(
    private val log: Logger,
    private val rentalComplexService: RentalComplexService,
) {

    @GetMapping
    fun getRentalComplexes(
        model: Model,
    ): String {
        model["rentalComplexes"] = rentalComplexService.getRentalComplexes()
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
    ): String = try {
        rentalComplexService.createRentalComplex(RentalComplexForm(name))
        "redirect:/rentalComplexes?success=create"
    } catch (e: Exception) {
        log.error(e.message, e)
        "redirect:/rentalComplexes?error=create"
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
    ): String = try {
        rentalComplexService.editRentalComplex(id, RentalComplexForm(name))
        "redirect:/rentalComplexes?success=edit"
    } catch (e: Exception) {
        log.error(e.message, e)
        "redirect:/rentalComplexes/show/$id?error=edit"
    }

    @PostMapping("delete/{id}")
    fun alterRentalComplex(
        @PathVariable("id") id: Long,
    ): String = try {
        rentalComplexService.deleteRentalComplex(id)
        "redirect:/rentalComplexes?success=delete"
    } catch (e: Exception) {
        log.error(e.message, e)
        "redirect:/rentalComplexes/show/$id?error=delete"
    }
}
