package de.cramer.nebenkosten.forms

data class FlatForm(
    val name: String,
    val area: Long,
    val order: Int = 0,
)
