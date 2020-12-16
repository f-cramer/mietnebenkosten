package de.cramer.nebenkosten.web.rest

import de.cramer.nebenkosten.entities.Bill
import de.cramer.nebenkosten.forms.BillForm
import de.cramer.nebenkosten.services.BillService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/bills")
class BillRestController(
    private val service: BillService
) {

    @GetMapping
    fun getAllBills(): List<Bill> = service.getBills()

    @PutMapping
    fun createBill(@RequestBody form: BillForm): Bill = service.createBill(form)

    @GetMapping("{id}")
    fun getBillById(@PathVariable("id") id: Long): Bill = service.getBill(id)

    @PostMapping("{id}")
    fun editBill(@PathVariable("id") id: Long, @RequestBody form: BillForm): Bill = service.editBill(id, form)

    @DeleteMapping("{id}")
    fun deleteBill(@PathVariable("id") id: Long): Unit = service.deleteBill(id)
}
