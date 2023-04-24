package de.cramer.nebenkosten.web.rest

import de.cramer.nebenkosten.entities.Contract
import de.cramer.nebenkosten.forms.ContractForm
import de.cramer.nebenkosten.services.ContractService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/contracts")
class ContractRestController(
    private val service: ContractService,
) {

    @GetMapping
    fun getAllContracts(): List<Contract> = service.getContracts()

    @PutMapping
    fun createContract(@RequestBody form: ContractForm): Contract = service.createContract(form)

    @GetMapping("{id}")
    fun getContractById(@PathVariable("id") id: Long): Contract = service.getContract(id)

    @PostMapping("{id}")
    fun editContract(@PathVariable("id") id: Long, @RequestBody form: ContractForm): Contract = service.editContract(id, form)

    @DeleteMapping("{id}")
    fun deleteContract(@PathVariable("id") id: Long): Unit = service.deleteContract(id)
}
