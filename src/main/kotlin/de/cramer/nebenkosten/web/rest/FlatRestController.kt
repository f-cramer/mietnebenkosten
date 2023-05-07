package de.cramer.nebenkosten.web.rest

import de.cramer.nebenkosten.entities.Flat
import de.cramer.nebenkosten.entities.RentalComplex
import de.cramer.nebenkosten.forms.FlatForm
import de.cramer.nebenkosten.services.FlatService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/flats")
class FlatRestController(
    private val service: FlatService,
) {

    @GetMapping
    fun getAllFlats(): List<Flat> = service.getFlats()

    @PutMapping
    fun createFlat(@RequestBody form: FlatForm, rentalComplex: RentalComplex): Flat = service.createFlat(form, rentalComplex)

    @GetMapping("{id}")
    fun getFlatById(@PathVariable("id") id: Long): Flat = service.getFlat(id)

    @PostMapping("{id}")
    fun editFlat(@PathVariable("id") id: Long, @RequestBody form: FlatForm, rentalComplex: RentalComplex): Flat = service.editFlat(id, form, rentalComplex)

    @DeleteMapping("{id}")
    fun deleteFlat(@PathVariable("id") id: Long): Unit = service.deleteFlat(id)
}
