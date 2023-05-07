package de.cramer.nebenkosten.forms

import de.cramer.nebenkosten.exceptions.BadRequestException
import java.time.LocalDate

class ContractForm(
    val flatId: Long,
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
