package de.cramer.nebenkosten.services

import de.cramer.nebenkosten.entities.Flat
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

    fun editFlat(id: Long, form: FlatForm): Flat =
        if (repository.existsById(id)) {
            repository.save(form.toFlat().copy(id = id))
        } else {
            throw ConflictException()
        }

    fun createFlat(form: FlatForm): Flat =
        repository.save(form.toFlat())

    fun deleteFlat(id: Long) {
        if (repository.existsById(id)) {
            repository.deleteById(id)
        } else {
            throw NotFoundException()
        }
    }

    fun FlatForm.toFlat() = Flat(
        0,
        name = name.trim(),
        order = order,
        area = area,
    )
}
