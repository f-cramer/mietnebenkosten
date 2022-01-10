package de.cramer.nebenkosten.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFoundException(message: String = "") : Exception(message) {
    companion object {
        private const val serialVersionUID: Long = -7723505798596879661L
    }
}
