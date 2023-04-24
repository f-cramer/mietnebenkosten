package de.cramer.nebenkosten.services

import de.cramer.nebenkosten.entities.Contract
import de.cramer.nebenkosten.entities.Contract_
import de.cramer.nebenkosten.entities.Flat
import de.cramer.nebenkosten.entities.LocalDatePeriod
import de.cramer.nebenkosten.exceptions.ConflictException
import de.cramer.nebenkosten.exceptions.NotFoundException
import de.cramer.nebenkosten.forms.ContractForm
import de.cramer.nebenkosten.repositories.ContractRepository
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import java.time.LocalDate
import kotlin.jvm.optionals.getOrElse

@Service
class ContractService(
    private val repository: ContractRepository,
    private val flatService: FlatService,
    private val tenantService: TenantService,
) {
    fun getContracts(includeClosed: Boolean = false): List<Contract> =
        (if (includeClosed) repository.findAll() else repository.findAll(overlappingDatePeriodSpecification(LocalDatePeriod(LocalDate.now()))))
            .sorted()

    fun getContract(id: Long): Contract = repository.findById(id)
        .getOrElse { throw NotFoundException() }

    fun editContract(id: Long, form: ContractForm): Contract =
        if (repository.existsById(id)) {
            repository.save(form.toContract().copy(id = id))
        } else {
            throw ConflictException()
        }

    fun createContract(form: ContractForm): Contract {
        val contract = form.toContract()

        if (getContractsByFlatAndPeriod(contract.flat, contract.period).isEmpty()) {
            return repository.save(contract)
        } else {
            throw ConflictException("error.contract.flatAlreadyInUse")
        }
    }

    fun getContractsByPeriod(period: LocalDatePeriod): List<Contract> =
        repository.findAll(overlappingDatePeriodSpecification(period))

    fun getContractsByFlatAndPeriod(flat: Flat, period: LocalDatePeriod): List<Contract> =
        repository.findAll(
            overlappingDatePeriodSpecification(period).and { root, _, criteriaBuilder ->
                criteriaBuilder.equal(root.get(Contract_.flat), flat)
            }
        )

    fun deleteContract(id: Long) {
        if (repository.existsById(id)) {
            repository.deleteById(id)
        } else {
            throw NotFoundException()
        }
    }

    fun ContractForm.toContract() = Contract(
        flat = flatService.getFlat(flatName),
        tenant = tenantService.getTenant(tenantId),
        period = LocalDatePeriod(start, end),
        persons = persons
    )

    private fun overlappingDatePeriodSpecification(period: LocalDatePeriod): Specification<Contract> = de.cramer.nebenkosten.extensions.overlappingDatePeriodSpecification(period) { it.get(Contract_.period) }
}
