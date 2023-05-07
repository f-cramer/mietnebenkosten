package de.cramer.nebenkosten.web.rest

import de.cramer.nebenkosten.entities.RentalComplex
import de.cramer.nebenkosten.entities.Tenant
import de.cramer.nebenkosten.forms.TenantForm
import de.cramer.nebenkosten.services.TenantService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/tenants")
class TenantRestController(
    private val service: TenantService,
) {

    @GetMapping
    fun getAllTenants(
        @RequestParam("includeHidden", defaultValue = "false") includeHidden: Boolean,
    ): List<Tenant> = service.getTenants(includeHidden)

    @PutMapping
    fun createTenant(@RequestBody form: TenantForm, rentalComplex: RentalComplex): Tenant = service.createTenant(form, rentalComplex)

    @GetMapping("{id}")
    fun getTenantById(@PathVariable("id") id: Long): Tenant = service.getTenant(id)

    @PostMapping("{id}")
    fun editTenant(@PathVariable("id") id: Long, @RequestBody form: TenantForm, rentalComplex: RentalComplex): Tenant = service.editTenant(id, form, rentalComplex)

    @DeleteMapping("{id}")
    fun deleteTenant(@PathVariable("id") id: Long): Unit = service.deleteTenant(id)
}
