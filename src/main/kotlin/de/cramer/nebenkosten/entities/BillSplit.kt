package de.cramer.nebenkosten.entities

data class BillSplit(
    val bill: Bill,
    val billing: Billing,
    val splittedAmount: MonetaryAmount
)
