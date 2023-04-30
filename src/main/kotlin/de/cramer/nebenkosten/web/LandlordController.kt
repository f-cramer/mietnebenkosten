package de.cramer.nebenkosten.web

import de.cramer.nebenkosten.extensions.set
import de.cramer.nebenkosten.forms.LandlordForm
import de.cramer.nebenkosten.services.LandlordService
import org.slf4j.Logger
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.time.Year

@Controller
@RequestMapping("landlords")
class LandlordController(
    private val log: Logger,
    private val landlordService: LandlordService,
) {

    @GetMapping
    fun getLandlords(
        model: Model,
    ): String {
        model["landlords"] = landlordService.getLandlords()
        return "landlords"
    }

    @GetMapping("create")
    @Suppress("FunctionOnlyReturningConstant")
    fun createLandlord(): String {
        return "landlord"
    }

    @PostMapping("create")
    fun createLandlord(
        @RequestParam("firstName") firstName: String,
        @RequestParam("lastName") lastName: String,
        @RequestParam("street") street: String,
        @RequestParam("houseNumber") houseNumber: Int,
        @RequestParam("zipCode") zipCode: String,
        @RequestParam("city") city: String,
        @RequestParam("country", required = false) country: String?,
        @RequestParam("iban") iban: String,
        @RequestParam("start") start: Year,
        @RequestParam("end", required = false) end: Year?,
    ): String = try {
        landlordService.createLandlord(LandlordForm(firstName, lastName, street, houseNumber, zipCode, city, country, iban, start, end))
        "redirect:/landlords"
    } catch (e: Exception) {
        log.debug(e.message, e)
        "redirect:/landlords?error=create"
    }

    @GetMapping("show/{id}")
    fun getLandlord(
        @PathVariable("id") id: Long,
        model: Model,
    ): String {
        model["landlord"] = landlordService.getLandlord(id)
        return "landlord"
    }

    @PostMapping("edit/{id}")
    fun editLandlord(
        @PathVariable("id") id: Long,
        @RequestParam("firstName") firstName: String,
        @RequestParam("lastName") lastName: String,
        @RequestParam("street") street: String,
        @RequestParam("houseNumber") houseNumber: Int,
        @RequestParam("zipCode") zipCode: String,
        @RequestParam("city") city: String,
        @RequestParam("country", required = false) country: String?,
        @RequestParam("iban") iban: String,
        @RequestParam("start") start: Year,
        @RequestParam("end", required = false) end: Year?,
    ): String = try {
        landlordService.editLandlord(id, LandlordForm(firstName, lastName, street, houseNumber, zipCode, city, country, iban, start, end))
        "redirect:/landlords"
    } catch (e: Exception) {
        log.error(e.message, e)
        "redirect:/landlords/show/$id?error=edit"
    }

    @PostMapping("delete/{id}")
    @Suppress("InstanceOfCheckForException")
    fun deleteLandlord(
        @PathVariable("id") id: Long,
        redirectAttributes: RedirectAttributes,
    ): String = try {
        landlordService.deleteLandlord(id)
        "redirect:/landlords"
    } catch (e: Exception) {
        if (e is DataIntegrityViolationException) {
            log.debug(e.message, e)
        } else {
            log.error(e.message, e)
        }
        redirectAttributes["error"] = "delete"
        redirectAttributes["errorMessage"] = e.message ?: ""
        "redirect:/landlords/show/$id"
    }
}
