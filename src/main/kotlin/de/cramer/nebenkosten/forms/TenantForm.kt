package de.cramer.nebenkosten.forms

import de.cramer.nebenkosten.entities.FormOfAddress
import de.cramer.nebenkosten.entities.Gender

data class TenantForm(
    val firstName: String,
    val lastName: String,
    val street: String,
    val houseNumber: Int,
    val zipCode: String,
    val city: String,
    val country: String? = null,
    val gender: Gender,
    val formOfAddress: FormOfAddress,
    val hidden: Boolean,
)
