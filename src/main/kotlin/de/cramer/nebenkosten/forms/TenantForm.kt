package de.cramer.nebenkosten.forms

data class TenantForm(
    val firstName: String,
    val lastName: String,
    val street: String,
    val houseNumber: Int,
    val zipCode: String,
    val city: String,
    val country: String? = null,
    val hidden: Boolean
)
