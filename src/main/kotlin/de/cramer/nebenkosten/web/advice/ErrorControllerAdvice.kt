package de.cramer.nebenkosten.web.advice

import de.cramer.nebenkosten.extensions.set
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestParam

@ControllerAdvice
class ErrorControllerAdvice {

    @ModelAttribute
    fun publishErrors(
        @RequestParam("error", defaultValue = "[]") error: List<String>,
        @RequestParam("errorMessage", defaultValue = "") errorMessage: String,
        model: Model,
    ) {
        model["saveError"] = error.contains("save")
        model["deletionError"] = error.contains("delete")
        model["errorMessage"] = errorMessage
    }
}
