package de.cramer.nebenkosten.services

import de.cramer.nebenkosten.entities.RentalComplex
import de.cramer.nebenkosten.entities.User
import de.cramer.nebenkosten.exceptions.ConflictException
import de.cramer.nebenkosten.exceptions.NotFoundException
import de.cramer.nebenkosten.forms.RentalComplexForm
import de.cramer.nebenkosten.repositories.RentalComplexRepository
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull

@Service
class RentalComplexService(
    private val repository: RentalComplexRepository,
) {
    fun getRentalComplexes(user: User): List<RentalComplex> = user.rentalComplexes.sorted()

    fun getRentalComplex(id: Long): RentalComplex = repository.findById(id)
        .getOrElse { throw NotFoundException() }

    fun editRentalComplex(id: Long, form: RentalComplexForm): RentalComplex {
        val complex = repository.findById(id).getOrNull()
        return if (complex != null) {
            complex.name = form.name
            repository.save(form.toRentalComplex().copy(id = id))
        } else {
            throw ConflictException()
        }
    }

    fun createRentalComplex(form: RentalComplexForm, user: User): RentalComplex {
        val complex = repository.save(form.toRentalComplex(user))
        user.rentalComplexes += complex
        return complex
    }

    fun deleteRentalComplex(id: Long) {
        repository.deleteById(id)
    }

    fun RentalComplexForm.toRentalComplex(user: User? = null) = RentalComplex(
        id = 0,
        name = name.trim(),
        users = if (user == null) mutableListOf() else mutableListOf(user),
    )
}
