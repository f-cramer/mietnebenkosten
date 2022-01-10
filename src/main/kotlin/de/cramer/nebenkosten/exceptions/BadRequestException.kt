package de.cramer.nebenkosten.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class BadRequestException(message: String = "", cause: Throwable? = null): RuntimeException(message, cause) {
    companion object {
        private const val serialVersionUID: Long = 7204926078698254586L
    }
}
