package de.cramer.nebenkosten.reports

import java.math.BigDecimal

data class BillingReport(
    val sender: ReportAddress,
    val receiver: ReportAddress,
    val note: ReportNote,
    val billings: List<ReportBilling>,
    val billingSum: ReportBillingSum,
    val generated: Boolean,
    val year: Int,
)

data class ReportAddress(
    val name: String,
    val street1: String,
    val street2: String,
    val city: String,
    val country: String,
)

data class ReportNote(
    val title: String,
    val text: String,
)

data class ReportBilling(
    val name: String,
    val splitUnit: String,
    val totalValue: BigDecimal?,
    val totalPrice: BigDecimal?,
    val partValue: BigDecimal?,
    val partPrice: BigDecimal,
)

data class ReportBillingSum(
    val totalPrice: BigDecimal,
)
