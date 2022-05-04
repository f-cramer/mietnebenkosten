package de.cramer.nebenkosten.forms

import java.time.Year

data class LandlordForm(
    val firstName: String,
    val lastName: String,
    val street: String,
    val houseNumber: Int,
    val zipCode: String,
    val city: String,
    val country: String? = null,
    val iban: String,
    val start: Year,
    val end: Year? = null,
)
