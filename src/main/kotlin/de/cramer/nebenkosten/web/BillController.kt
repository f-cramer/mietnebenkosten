package de.cramer.nebenkosten.web

import de.cramer.nebenkosten.exceptions.BadRequestException
import de.cramer.nebenkosten.forms.BillForm
import de.cramer.nebenkosten.services.BillService
import org.slf4j.Logger
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.time.LocalDate

@Controller
@RequestMapping("bills")
class BillController(
    private val log: Logger,
    private val billService: BillService
) {

    @GetMapping("")
    fun getBills(
        @RequestParam(name = "includeClosed", defaultValue = "false") includeClosed: Boolean,
        model: Model
    ): String {
        model.addAttribute("includeClosed", includeClosed)
        return "bills"
    }

    @GetMapping("create")
    fun createBill(): String {
        return "bill"
    }

    @PostMapping("create")
    fun createBill(
        @RequestParam("description") description: String,
        @RequestParam("price") price: Long,
        @RequestParam("start") start: LocalDate,
        @RequestParam("end", required = false) end: LocalDate?,
        redirectAttributes: RedirectAttributes
    ): String = try {
        BillForm(description, price, start, end).apply {
            validate()
            billService.createBill(this)
        }
        "redirect:/bills"
    } catch (e: Exception) {
        log.error(e.message, e)
        redirectAttributes.addAttribute("error", "create")
        redirectAttributes.addAttribute("errorMessage", e.message)
        "redirect:/bills"
    }

    @GetMapping("show/{id}")
    fun getBill(
        @PathVariable("id") id: Long,
        model: Model
    ): String {
        model.addAttribute("id", id)
        return "bill"
    }

    @PostMapping("edit/{id}")
    fun editBill(
        @PathVariable("id") id: Long,
        @RequestParam("description") description: String,
        @RequestParam("price") price: Long,
        @RequestParam("start") start: LocalDate,
        @RequestParam("end", required = false) end: LocalDate?,
        redirectAttributes: RedirectAttributes
    ): String = try {
        BillForm(description, price, start, end).apply {
            validate()
            billService.editBill(id, this)
        }
        "redirect:/bills"
    } catch (e: BadRequestException) {
        log.debug(e.message, e)
        redirectAttributes.addAttribute("error", "badRequest")
        redirectAttributes.addFlashAttribute("errorMessage", e.message)
        "redirect:/bills/show/$id"
    } catch (e: Exception) {
        log.error(e.message, e)
        redirectAttributes.addAttribute("error", "edit")
        redirectAttributes.addFlashAttribute("errorMessage", e.message)
        "redirect:/bills/show/$id"
    }

    @PostMapping("delete/{id}")
    fun alterBill(
        @PathVariable("id") id: Long,
        redirectAttributes: RedirectAttributes
    ): String = try {
        billService.deleteBill(id)
        "redirect:/bills"
    } catch (e: Exception) {
        log.error(e.message, e)
        redirectAttributes.addAttribute("error", "delete")
        redirectAttributes.addFlashAttribute("errorMessage", e.message)
        "redirect:/bills/show/$id"
    }
}
