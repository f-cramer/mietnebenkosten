package de.cramer.nebenkosten.config.rentalcomplex

import de.cramer.nebenkosten.exceptions.BadRequestException
import de.cramer.nebenkosten.extensions.currentUser
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.util.WebUtils

@Component
class RentalComplexChangeInterceptor : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val user = currentUser ?: return true
        val rentalComplexId = request.getParameter(PARAMETER_NAME)?.toLongOrNull() ?: return true

        if (user.rentalComplexes.none { it.id == rentalComplexId }) return true

        try {
            WebUtils.setSessionAttribute(request, RentalComplexResolver.ATTRIBUTE_NAME, rentalComplexId.toLong())
        } catch (e: Exception) {
            throw BadRequestException(e.message ?: "", e)
        }

        return true
    }

    companion object {
        const val PARAMETER_NAME: String = "rentalComplexId"
    }
}
