package de.cramer.nebenkosten.web.advice

import de.cramer.nebenkosten.extensions.set
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute
import java.time.Year

@ControllerAdvice
class YearControllerAdvice {

    @ModelAttribute
    fun publishYear(
        year: Year,
        model: Model,
    ) {
        model["year"] = year.toString()
    }
}
