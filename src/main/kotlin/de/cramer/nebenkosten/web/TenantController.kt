package de.cramer.nebenkosten.web

import de.cramer.nebenkosten.entities.FormOfAddress
import de.cramer.nebenkosten.entities.Gender
import de.cramer.nebenkosten.extensions.set
import de.cramer.nebenkosten.forms.TenantForm
import de.cramer.nebenkosten.services.TenantService
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

@Controller
@RequestMapping("tenants")
class TenantController(
    private val log: Logger,
    private val tenantService: TenantService,
) {

    @GetMapping
    fun getTenants(
        @RequestParam("includeHidden", defaultValue = "false") includeHidden: Boolean,
        model: Model,
    ): String {
        model["includeHidden"] = includeHidden
        model["tenants"] = tenantService.getTenants(includeHidden)
        return "tenants"
    }

    @GetMapping("create")
    fun createTenant(): String {
        return "tenant"
    }

    @PostMapping("create")
    fun createTenant(
        @RequestParam("firstName") firstName: String,
        @RequestParam("lastName") lastName: String,
        @RequestParam("street") street: String,
        @RequestParam("houseNumber") houseNumber: Int,
        @RequestParam("zipCode") zipCode: String,
        @RequestParam("city") city: String,
        @RequestParam("country", required = false) country: String?,
        @RequestParam("gender") gender: Gender,
        @RequestParam("formOfAddress") formOfAddress: FormOfAddress,
        @RequestParam("hidden", defaultValue = "false") hidden: Boolean,
    ): String = try {
        tenantService.createTenant(TenantForm(firstName, lastName, street, houseNumber, zipCode, city, country, gender, formOfAddress, hidden))
        "redirect:/tenants"
    } catch (e: Exception) {
        "redirect:/tenants?error=create"
    }

    @GetMapping("show/{id}")
    fun getTenant(
        @PathVariable("id") id: Long,
        model: Model,
    ): String {
        model["tenant"] = tenantService.getTenant(id)
        return "tenant"
    }

    @PostMapping("edit/{id}")
    fun editTenant(
        @PathVariable("id") id: Long,
        @RequestParam("firstName") firstName: String,
        @RequestParam("lastName") lastName: String,
        @RequestParam("street") street: String,
        @RequestParam("houseNumber") houseNumber: Int,
        @RequestParam("zipCode") zipCode: String,
        @RequestParam("city") city: String,
        @RequestParam("country", required = false) country: String?,
        @RequestParam("gender") gender: Gender,
        @RequestParam("formOfAddress") formOfAddress: FormOfAddress,
        @RequestParam("hidden", defaultValue = "false") hidden: Boolean,
    ): String = try {
        tenantService.editTenant(id, TenantForm(firstName, lastName, street, houseNumber, zipCode, city, country, gender, formOfAddress, hidden))
        "redirect:/tenants"
    } catch (e: Exception) {
        log.error(e.message, e)
        "redirect:/tenants/show/$id?error=edit"
    }

    @PostMapping("delete/{id}")
    fun deleteTenant(
        @PathVariable("id") id: Long,
        redirectAttributes: RedirectAttributes,
    ): String = try {
        tenantService.deleteTenant(id)
        "redirect:/tenants"
    } catch (e: Exception) {
        if (e is DataIntegrityViolationException) {
            log.debug(e.message, e)
        } else {
            log.error(e.message, e)
        }
        redirectAttributes["error"] = "delete"
        redirectAttributes["errorMessage"] = e.message ?: ""
        "redirect:/tenants/show/$id"
    }
}
