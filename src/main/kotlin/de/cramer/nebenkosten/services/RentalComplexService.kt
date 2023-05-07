package de.cramer.nebenkosten.services

import de.cramer.nebenkosten.entities.RentalComplex
import de.cramer.nebenkosten.exceptions.ConflictException
import de.cramer.nebenkosten.exceptions.NotFoundException
import de.cramer.nebenkosten.forms.RentalComplexForm
import de.cramer.nebenkosten.repositories.RentalComplexRepository
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse

@Service
class RentalComplexService(
    private val repository: RentalComplexRepository,
) {
    fun getRentalComplexes(): List<RentalComplex> = repository.findAll().sorted()

    fun getRentalComplex(id: Long): RentalComplex = repository.findById(id)
        .getOrElse { throw NotFoundException() }

    fun editRentalComplex(id: Long, form: RentalComplexForm): RentalComplex =
        if (repository.existsById(id)) {
            repository.save(form.toRentalComplex().copy(id = id))
        } else {
            throw ConflictException()
        }

    fun createRentalComplex(form: RentalComplexForm): RentalComplex =
        repository.save(form.toRentalComplex())

    fun deleteRentalComplex(id: Long) {
        repository.deleteById(id)
    }

    fun RentalComplexForm.toRentalComplex() = RentalComplex(
        id = 0,
        name = name.trim(),
    )
}
