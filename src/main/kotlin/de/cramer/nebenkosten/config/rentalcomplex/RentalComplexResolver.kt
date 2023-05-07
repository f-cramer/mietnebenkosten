package de.cramer.nebenkosten.config.rentalcomplex

import de.cramer.nebenkosten.entities.RentalComplex
import de.cramer.nebenkosten.extensions.currentRequest
import de.cramer.nebenkosten.extensions.currentUser
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import org.springframework.web.util.WebUtils
import kotlin.reflect.jvm.jvmName

@Component
class RentalComplexResolver {

    val rentalComplexOverride = ThreadLocal<RentalComplex>()

    fun setRentalComplexOverride(rentalComplex: RentalComplex) {
        rentalComplexOverride.set(rentalComplex)
    }

    fun clearRentalComplexOverride() {
        rentalComplexOverride.remove()
    }

    fun getCurrentRentalComplex(): RentalComplex? {
        return getCurrentRentalComplexImpl { currentRequest }
    }

    fun getCurrentRentalComplex(request: HttpServletRequest): RentalComplex? {
        return getCurrentRentalComplexImpl { request }
    }

    private fun getCurrentRentalComplexImpl(request: () -> HttpServletRequest?): RentalComplex? {
        return rentalComplexOverride.get() ?: getRentalComplexFromRequest(request())
    }

    private fun getRentalComplexFromRequest(request: HttpServletRequest?): RentalComplex? {
        if (request == null) return null
        val rawRentalComplexId = WebUtils.getSessionAttribute(request, ATTRIBUTE_NAME) as? Number ?: return null
        val currentUser = currentUser ?: return null
        val rentalComplexId = rawRentalComplexId.toLong()
        return currentUser.rentalComplexes.first { it.id == rentalComplexId }
    }

    companion object {
        val ATTRIBUTE_NAME: String = "${RentalComplexMethodArgumentResolver::class.jvmName}.rentalCompexId"
    }
}
