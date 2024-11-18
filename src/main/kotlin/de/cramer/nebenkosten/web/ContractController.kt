package de.cramer.nebenkosten.web

import de.cramer.nebenkosten.entities.Contract
import de.cramer.nebenkosten.entities.Flat
import de.cramer.nebenkosten.entities.LocalDatePeriod
import de.cramer.nebenkosten.exceptions.BadRequestException
import de.cramer.nebenkosten.extensions.set
import de.cramer.nebenkosten.forms.ContractForm
import de.cramer.nebenkosten.services.ContractService
import de.cramer.nebenkosten.services.FlatService
import de.cramer.nebenkosten.services.TenantService
import org.slf4j.Logger
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.time.LocalDate
import java.time.Year

@Controller
@RequestMapping("contracts")
class ContractController(
    private val log: Logger,
    private val contractService: ContractService,
    private val flatService: FlatService,
    private val tenantService: TenantService,
) {

    @GetMapping
    fun getContracts(
        year: Year,
        @RequestParam(name = "error", required = false) error: String?,
        @RequestParam(name = "errorMessage", required = false) errorMessage: String?,
        model: Model,
    ): String {
        val flats = flatService.getFlats().toMutableList()
        val contractsByFlat = contractService.getContractsByPeriod(LocalDatePeriod.ofYear(year))
            .groupBy { it.flat }
            .mapValues { it.value.sorted() }
            .asSequence()
            .map { ContractsByFlat(it.key, it.value) }
            .onEach { flats -= it.flat }
            .toList().asSequence() // evaluate onEach eagerly
            .plus(flats.map { ContractsByFlat(it, emptyList()) })
            .sorted()
            .toList()
        model["contractsByFlat"] = contractsByFlat
        model["error"] = error
        model["errorMessage"] = errorMessage
        return "contracts"
    }

    @GetMapping("create")
    fun createContract(
        @RequestParam(name = "flat", required = false) flatName: String?,
        model: Model,
    ): String {
        val flats = flatService.getFlats()
        val flat = if (flatName != null) flats.firstOrNull { it.name == flatName } else null
        if (flat != null) {
            model["selectedFlat"] = flat
        }
        model["flats"] = flats
        model["tenants"] = tenantService.getTenants(false)
        return "contract"
    }

    @PostMapping("create")
    fun createContract(
        @RequestParam("flat") flatId: Long,
        @RequestParam("tenant") tenantId: Long,
        @RequestParam("persons") persons: Int,
        @RequestParam("start") start: LocalDate,
        @RequestParam("end", required = false) end: LocalDate?,
        redirectAttributes: RedirectAttributes,
    ): String = try {
        ContractForm(flatId, tenantId, persons, start, end).apply {
            validate()
            contractService.createContract(this)
        }
        "redirect:/contracts"
    } catch (e: Exception) {
        log.error(e.message, e)
        redirectAttributes["error"] = "create"
        redirectAttributes["errorMessage"] = e.message ?: ""
        "redirect:/contracts"
    }

    @GetMapping("show/{id}")
    fun getContract(
        @PathVariable("id") id: Long,
        model: Model,
    ): String {
        model["contract"] = contractService.getContract(id)
        model["flats"] = flatService.getFlats()
        model["tenants"] = tenantService.getTenants(false)
        return "contract"
    }

    @PostMapping("edit/{id}")
    fun editContract(
        @PathVariable("id") id: Long,
        @RequestParam("flat") flatId: Long,
        @RequestParam("tenant") tenantId: Long,
        @RequestParam("persons") persons: Int,
        @RequestParam("start") start: LocalDate,
        @RequestParam("end", required = false) end: LocalDate?,
        redirectAttributes: RedirectAttributes,
    ): String = try {
        ContractForm(flatId, tenantId, persons, start, end).apply {
            validate()
            contractService.editContract(id, this)
        }
        "redirect:/contracts"
    } catch (e: BadRequestException) {
        log.debug(e.message, e)
        redirectAttributes["error"] = "badRequest"
        redirectAttributes["errorMessage"] = e.message ?: ""
        "redirect:/contracts/show/$id"
    } catch (e: Exception) {
        log.error(e.message, e)
        redirectAttributes["error"] = "edit"
        redirectAttributes["errorMessage"] = e.message ?: ""
        "redirect:/contracts/show/$id"
    }

    @PostMapping("delete/{id}")
    fun alterContract(
        @PathVariable("id") id: Long,
        redirectAttributes: RedirectAttributes,
    ): String = try {
        contractService.deleteContract(id)
        "redirect:/contracts"
    } catch (e: Exception) {
        log.error(e.message, e)
        redirectAttributes["error"] = "delete"
        redirectAttributes["errorMessage"] = e.message ?: ""
        "redirect:/contracts/show/$id"
    }

    private data class ContractsByFlat(
        val flat: Flat,
        val contracts: List<Contract>,
    ) : Comparable<ContractsByFlat> {

        override fun compareTo(other: ContractsByFlat) =
            COMPARATOR.compare(this, other)

        companion object {

            private val COMPARATOR = compareBy<ContractsByFlat> { it.flat }
        }
    }
}
