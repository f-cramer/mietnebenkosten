package de.cramer.nebenkosten.services

import de.cramer.nebenkosten.entities.Flat
import de.cramer.nebenkosten.entities.LocalDatePeriod
import de.cramer.nebenkosten.entities.Rental
import de.cramer.nebenkosten.entities.Rental_
import de.cramer.nebenkosten.exceptions.ConflictException
import de.cramer.nebenkosten.exceptions.NotFoundException
import de.cramer.nebenkosten.forms.RentalForm
import de.cramer.nebenkosten.repositories.RentalRepository
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class RentalService(
    private val repository: RentalRepository,
    private val flatService: FlatService,
    private val tenantService: TenantService,
) {
    fun getRentals(includeClosed: Boolean = false): List<Rental> =
        (if (includeClosed) repository.findAll() else repository.findAll(overlappingDatePeriodSpecification(LocalDatePeriod(LocalDate.now()))))
            .sorted()

    fun getRental(id: Long): Rental = repository.findById(id)
        .orElseThrow { NotFoundException() }

    fun editRental(id: Long, form: RentalForm): Rental =
        if (repository.existsById(id)) {
            repository.save(form.toRental().copy(id = id))
        } else {
            throw ConflictException()
        }

    fun createRental(form: RentalForm): Rental {
        val rental = form.toRental()

        if (getRentalsByFlatAndPeriod(rental.flat, rental.period).isEmpty()) {
            return repository.save(rental)
        } else {
            throw ConflictException("error.rental.flatAlreadyInUse")
        }
    }

    fun getRentalsByPeriod(period: LocalDatePeriod): List<Rental> =
        repository.findAll(overlappingDatePeriodSpecification(period))

    fun getRentalsByFlatAndPeriod(flat: Flat, period: LocalDatePeriod): List<Rental> =
        repository.findAll(
            overlappingDatePeriodSpecification(period).and { root, _, criteriaBuilder ->
                criteriaBuilder.equal(root.get(Rental_.flat), flat)
            }
        )

    fun deleteRental(id: Long) {
        if (repository.existsById(id)) {
            repository.deleteById(id)
        } else {
            throw NotFoundException()
        }
    }

    fun RentalForm.toRental() = Rental(
        flat = flatService.getFlat(flatName),
        tenant = tenantService.getTenant(tenantId),
        period = LocalDatePeriod(start, end),
        persons = persons
    )

    private fun overlappingDatePeriodSpecification(period: LocalDatePeriod): Specification<Rental> = de.cramer.nebenkosten.extensions.overlappingDatePeriodSpecification(period) { it.get(Rental_.period) }
}
