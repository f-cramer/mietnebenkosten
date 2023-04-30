package de.cramer.nebenkosten.web

import de.cramer.nebenkosten.extensions.set
import de.cramer.nebenkosten.forms.FlatForm
import de.cramer.nebenkosten.services.FlatService
import org.slf4j.Logger
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("flats")
class FlatController(
    private val log: Logger,
    private val flatService: FlatService,
) {

    @GetMapping
    fun getFlats(
        model: Model,
    ): String {
        model["flats"] = flatService.getFlats()
        return "flats"
    }

    @GetMapping("create")
    @Suppress("FunctionOnlyReturningConstant")
    fun createFlat(): String {
        return "flat"
    }

    @PostMapping("create")
    fun createFlat(
        @RequestParam("name") name: String,
        @RequestParam("area") area: Long,
        @RequestParam("order") order: Int,
    ): String = try {
        flatService.createFlat(FlatForm(name, area, order))
        "redirect:/flats?success=create"
    } catch (e: Exception) {
        log.error(e.message, e)
        "redirect:/flats?error=create"
    }

    @GetMapping("show/{name}")
    fun getFlat(
        @PathVariable("name") name: String,
        model: Model,
    ): String {
        model["flat"] = flatService.getFlat(name)
        return "flat"
    }

    @PostMapping("edit/{name}")
    fun editFlat(
        @PathVariable("name") originalName: String,
        @RequestParam("name") name: String,
        @RequestParam("area") area: Long,
        @RequestParam("order") order: Int,
    ): String = try {
        flatService.editFlat(originalName, FlatForm(name, area, order))
        "redirect:/flats?success=edit"
    } catch (e: Exception) {
        log.error(e.message, e)
        "redirect:/flats/show/$name?error=edit"
    }

    @PostMapping("delete/{name}")
    fun alterFlat(
        @PathVariable("name") name: String,
    ): String = try {
        flatService.deleteFlat(name)
        "redirect:/flats?success=delete"
    } catch (e: Exception) {
        log.error(e.message, e)
        "redirect:/flats/show/$name?error=delete"
    }
}
