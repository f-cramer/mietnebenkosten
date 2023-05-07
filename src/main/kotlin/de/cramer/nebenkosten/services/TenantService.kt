package de.cramer.nebenkosten.services

import de.cramer.nebenkosten.entities.Address
import de.cramer.nebenkosten.entities.RentalComplex
import de.cramer.nebenkosten.entities.Tenant
import de.cramer.nebenkosten.exceptions.ConflictException
import de.cramer.nebenkosten.exceptions.NotFoundException
import de.cramer.nebenkosten.forms.TenantForm
import de.cramer.nebenkosten.jpa.specifications.TenantsByHiddenSpecification
import de.cramer.nebenkosten.repositories.TenantRepository
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse

@Service
class TenantService(
    private val repository: TenantRepository,
) {
    fun getTenants(includeHidden: Boolean = false): List<Tenant> =
        (if (includeHidden) repository.findAll() else repository.findAll(TenantsByHiddenSpecification(false)))
            .sorted()

    fun getTenant(id: Long): Tenant = repository.findById(id)
        .getOrElse { throw NotFoundException() }

    fun editTenant(id: Long, form: TenantForm, rentalComplex: RentalComplex): Tenant =
        if (repository.existsById(id)) {
            repository.save(form.toTenant(rentalComplex).copy(id = id))
        } else {
            throw ConflictException()
        }

    fun createTenant(form: TenantForm, rentalComplex: RentalComplex): Tenant =
        repository.save(form.toTenant(rentalComplex))

    fun TenantForm.toTenant(rentalComplex: RentalComplex) = Tenant(
        firstName = firstName.trim(),
        lastName = lastName.trim(),
        address = Address(
            street = street.trim(),
            houseNumber = houseNumber,
            zipCode = zipCode.trim(),
            city = city.trim(),
            country = if (country.isNullOrEmpty()) null else country.trim(),
        ),
        gender = gender,
        formOfAddress = formOfAddress,
        hidden = hidden,
        rentalComplex = rentalComplex,
    )

    fun deleteTenant(id: Long) {
        if (repository.existsById(id)) {
            repository.deleteById(id)
        } else {
            throw NotFoundException()
        }
    }
}
