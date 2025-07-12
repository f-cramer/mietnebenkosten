package de.cramer.nebenkosten.web.advice

import de.cramer.nebenkosten.extensions.set
import jakarta.servlet.http.HttpServletRequest
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

@ControllerAdvice
class RequestParametersControllerAdvice {

    @ModelAttribute
    fun publishRequest(
        request: HttpServletRequest,
        model: Model,
    ) {
        model["parameters"] = request.parameterMap
    }
}
