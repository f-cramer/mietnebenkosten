package de.cramer.nebenkosten.forms

import de.cramer.nebenkosten.exceptions.BadRequestException
import java.time.LocalDate

data class BillForm(
    val description: String,
    val priceInCent: Long,
    val start: LocalDate,
    val end: LocalDate? = null
) {
    fun validate() {
        if (end != null && start > end) {
            throw BadRequestException();
        }
    }
}
