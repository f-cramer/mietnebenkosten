package de.cramer.nebenkosten.forms

import java.time.LocalDate
import de.cramer.nebenkosten.exceptions.BadRequestException

class RentalForm(
    val flatName: String,
    val tenantId: Long,
    val persons: Int,
    val start: LocalDate,
    val end: LocalDate? = null,
) {
    fun validate() {
        if (end != null && start > end) {
            throw BadRequestException()
        }
    }
}
