package de.cramer.nebenkosten.entities

data class BillingPeriod(
    val rental: Rental,
    val period: LocalDatePeriod
)
