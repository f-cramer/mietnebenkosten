package de.cramer.nebenkosten.services

import de.cramer.nebenkosten.entities.Flat
import de.cramer.nebenkosten.exceptions.ConflictException
import de.cramer.nebenkosten.exceptions.NotFoundException
import de.cramer.nebenkosten.forms.FlatForm
import de.cramer.nebenkosten.repositories.FlatRepository
import org.springframework.stereotype.Service

@Service
class FlatService(
    private val repository: FlatRepository,
) {
    fun getFlats(): List<Flat> = repository.findAll().sorted()

    fun getFlat(name: String): Flat = repository.findById(name)
        .orElseThrow { NotFoundException() }

    fun editFlat(name: String, form: FlatForm): Flat =
        if (repository.existsById(name)) {
            repository.save(form.toFlat().copy(name = name))
        } else {
            throw ConflictException()
        }

    fun createFlat(form: FlatForm): Flat =
        if (repository.existsById(form.name)) {
            throw ConflictException()
        } else {
            repository.save(form.toFlat())
        }

    fun deleteFlat(name: String) {
        if (repository.existsById(name)) {
            repository.deleteById(name)
        } else {
            throw NotFoundException()
        }
    }

    fun FlatForm.toFlat() = Flat(
        name = name.trim(),
        order = order,
        area = area
    )
}
