package de.cramer.nebenkosten.web.rest

import de.cramer.nebenkosten.entities.Flat
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
    fun createFlat(@RequestBody form: FlatForm): Flat = service.createFlat(form)

    @GetMapping("{name}")
    fun getFlatById(@PathVariable("name") name: String): Flat = service.getFlat(name)

    @PostMapping("{name}")
    fun editFlat(@PathVariable("name") name: String, @RequestBody form: FlatForm): Flat = service.editFlat(name, form)

    @DeleteMapping("{name}")
    fun deleteFlat(@PathVariable("name") name: String): Unit = service.deleteFlat(name)
}
