package de.cramer.nebenkosten.config.year

import java.time.Year
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import de.cramer.nebenkosten.exceptions.BadRequestException
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.util.WebUtils

@Component
class YearChangeInterceptor : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val newYear = request.getParameter(PARAMETER_NAME)
        if (newYear != null) {
            try {
                WebUtils.setSessionAttribute(request, YearResolver.ATTRIBUTE_NAME, Year.of(newYear.toInt()))
            } catch (e: Exception) {
                throw BadRequestException(e.message ?: "", e)
            }
        }

        return true
    }

    companion object {
        const val PARAMETER_NAME: String = "year"
    }
}
