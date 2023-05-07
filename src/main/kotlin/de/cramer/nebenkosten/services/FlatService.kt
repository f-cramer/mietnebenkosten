package de.cramer.nebenkosten.services

import de.cramer.nebenkosten.entities.Flat
import de.cramer.nebenkosten.entities.RentalComplex
import de.cramer.nebenkosten.exceptions.ConflictException
import de.cramer.nebenkosten.exceptions.NotFoundException
import de.cramer.nebenkosten.forms.FlatForm
import de.cramer.nebenkosten.repositories.FlatRepository
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse

@Service
class FlatService(
    private val repository: FlatRepository,
) {
    fun getFlats(): List<Flat> = repository.findAll().sorted()

    fun getFlat(id: Long): Flat = repository.findById(id)
        .getOrElse { throw NotFoundException() }

    fun editFlat(id: Long, form: FlatForm, rentalComplex: RentalComplex): Flat =
        if (repository.existsById(id)) {
            repository.save(form.toFlat(rentalComplex).copy(id = id))
        } else {
            throw ConflictException()
        }

    fun createFlat(form: FlatForm, rentalComplex: RentalComplex): Flat =
        repository.save(form.toFlat(rentalComplex))

    fun deleteFlat(id: Long) {
        if (repository.existsById(id)) {
            repository.deleteById(id)
        } else {
            throw NotFoundException()
        }
    }

    fun FlatForm.toFlat(rentalComplex: RentalComplex) = Flat(
        0,
        name = name.trim(),
        order = order,
        area = area,
        rentalComplex = rentalComplex,
    )
}
