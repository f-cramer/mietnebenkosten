package de.cramer.nebenkosten.entities

data class BillingPeriod(
    val contract: Contract,
    val period: LocalDatePeriod,
)
