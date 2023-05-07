package de.cramer.nebenkosten.web.advice

import de.cramer.nebenkosten.entities.RentalComplex
import de.cramer.nebenkosten.extensions.currentUser
import de.cramer.nebenkosten.extensions.set
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

@ControllerAdvice
class SecurityControllerAdvice {

    @ModelAttribute
    fun publishUser(
        model: Model,
    ) {
        currentUser?.let {
            model["user"] = it
        }
    }

    @ModelAttribute
    fun publishCurrentRentalComplex(
        rentalComplex: RentalComplex?,
        model: Model,
    ) {
        if (rentalComplex != null) {
            model["currentRentalComplex"] = rentalComplex
        }
    }
}
