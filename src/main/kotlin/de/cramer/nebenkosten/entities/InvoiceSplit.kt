package de.cramer.nebenkosten.entities

import java.math.BigDecimal

data class InvoiceSplit(
    val invoice: Invoice,
    val billing: BillingPeriod,
    val totalValue: BigDecimal?,
    val splittedValue: BigDecimal?,
    val splittedAmount: MonetaryAmount,
)
