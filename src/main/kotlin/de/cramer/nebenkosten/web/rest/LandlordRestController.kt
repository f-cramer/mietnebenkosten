package de.cramer.nebenkosten.web.rest

import de.cramer.nebenkosten.entities.Landlord
import de.cramer.nebenkosten.entities.RentalComplex
import de.cramer.nebenkosten.forms.LandlordForm
import de.cramer.nebenkosten.services.LandlordService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/landlords")
class LandlordRestController(
    private val service: LandlordService,
) {

    @GetMapping
    fun getAllLandlords(): List<Landlord> = service.getLandlords()

    @PutMapping
    fun createLandlord(@RequestBody form: LandlordForm, rentalComplex: RentalComplex): Landlord = service.createLandlord(form, rentalComplex)

    @GetMapping("{id}")
    fun getLandlordById(@PathVariable("id") id: Long): Landlord = service.getLandlord(id)

    @PostMapping("{id}")
    fun editLandlord(@PathVariable("id") id: Long, @RequestBody form: LandlordForm, rentalComplex: RentalComplex): Landlord = service.editLandlord(id, form, rentalComplex)

    @DeleteMapping("{id}")
    fun deleteLandlord(@PathVariable("id") id: Long): Unit = service.deleteLandlord(id)
}
