package de.cramer.nebenkosten.forms

import de.cramer.nebenkosten.entities.SplitAlgorithmType
import de.cramer.nebenkosten.exceptions.BadRequestException
import java.time.LocalDate

data class InvoiceForm(
    val description: String,
    val priceInCent: Long,
    val type: InvoiceType,
    val splitAlgorithmType: SplitAlgorithmType?,
    val contract: Long?,
    val order: Int,
    val start: LocalDate,
    val end: LocalDate? = null,
) {
    fun validate() {
        if (end != null && start > end) {
            throw BadRequestException()
        }
        if (type == InvoiceType.General && splitAlgorithmType == null) {
            throw BadRequestException()
        }
        if (type == InvoiceType.Contract && contract == null) {
            throw BadRequestException()
        }
    }
}

enum class InvoiceType {
    General,
    Contract,
}
