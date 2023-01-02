package de.cramer.nebenkosten.services

import de.cramer.nebenkosten.entities.Address
import de.cramer.nebenkosten.entities.Landlord
import de.cramer.nebenkosten.entities.Landlord_
import de.cramer.nebenkosten.entities.LocalDatePeriod
import de.cramer.nebenkosten.entities.YearPeriod
import de.cramer.nebenkosten.exceptions.ConflictException
import de.cramer.nebenkosten.exceptions.NotFoundException
import de.cramer.nebenkosten.extensions.overlappingYearPeriodSpecification
import de.cramer.nebenkosten.forms.LandlordForm
import de.cramer.nebenkosten.repositories.LandlordRepository
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import java.time.Year
import kotlin.jvm.optionals.getOrElse

@Service
class LandlordService(
    private val repository: LandlordRepository,
) {
    fun getLandlords(): List<Landlord> =
        repository.findAll().sorted()

    fun getLandlordsByTimePeriod(period: LocalDatePeriod): List<Landlord> {
        val yearPeriod = YearPeriod(Year.from(period.start), period.end?.let { Year.from(it) })
        return repository.findAll(overlappingYearPeriodSpecification(yearPeriod)).sorted()
    }

    fun getLandlord(id: Long): Landlord =
        repository.findById(id)
            .getOrElse { throw NotFoundException() }

    fun editLandlord(id: Long, form: LandlordForm): Landlord =
        if (repository.existsById(id)) {
            repository.save(form.toLandlord(id))
        } else {
            throw ConflictException()
        }

    fun createLandlord(form: LandlordForm): Landlord =
        repository.save(form.toLandlord(0))

    fun deleteLandlord(id: Long) {
        if (repository.existsById(id)) {
            repository.deleteById(id)
        } else {
            throw NotFoundException()
        }
    }

    private fun LandlordForm.toLandlord(id: Long) = Landlord(
        id = id,
        firstName = firstName.trim(),
        lastName = lastName.trim(),
        address = Address(
            street = street.trim(),
            houseNumber = houseNumber,
            zipCode = zipCode.trim(),
            city = city.trim(),
            country = if (country == null || country.isEmpty()) null else country.trim()
        ),
        iban = iban,
        period = YearPeriod(start, end),
    )

    private fun overlappingYearPeriodSpecification(period: YearPeriod): Specification<Landlord> = overlappingYearPeriodSpecification(period) { it.get(Landlord_.period) }
}
