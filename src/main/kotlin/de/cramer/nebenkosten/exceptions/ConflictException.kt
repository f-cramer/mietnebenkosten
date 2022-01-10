package de.cramer.nebenkosten.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.CONFLICT)
class ConflictException(message: String = "") : Exception(message) {
    companion object {
        private const val serialVersionUID: Long = 5385795345934092746L
    }
}
