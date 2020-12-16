package de.cramer.nebenkosten.web.rest

import de.cramer.nebenkosten.entities.Rental
import de.cramer.nebenkosten.forms.RentalForm
import de.cramer.nebenkosten.services.RentalService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/rentals")
class RentalRestController(
    private val service: RentalService
) {

    @GetMapping
    fun getAllRentals(): List<Rental> = service.getRentals()

    @PutMapping
    fun createRental(@RequestBody form: RentalForm): Rental = service.createRental(form)

    @GetMapping("{id}")
    fun getRentalById(@PathVariable("id") id: Long): Rental = service.getRental(id)

    @PostMapping("{id}")
    fun editRental(@PathVariable("id") id: Long, @RequestBody form: RentalForm): Rental = service.editRental(id, form)

    @DeleteMapping("{id}")
    fun deleteRental(@PathVariable("id") id: Long): Unit = service.deleteRental(id)
}
