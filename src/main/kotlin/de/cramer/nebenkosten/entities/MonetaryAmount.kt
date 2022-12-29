package de.cramer.nebenkosten.entities

import de.cramer.nebenkosten.extensions.ONE
import de.cramer.nebenkosten.extensions.ZERO
import de.cramer.nebenkosten.extensions.toInternalBigDecimal
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.math.RoundingMode.CEILING
import java.math.RoundingMode.DOWN
import java.math.RoundingMode.FLOOR
import java.math.RoundingMode.UP
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.pow

@Embeddable
data class MonetaryAmount(
    @Column(name = "monetary_amount")
    val amount: BigDecimal = ZERO,
) : Comparable<MonetaryAmount> {
    constructor(amount: Long) : this(amount.toInternalBigDecimal())
    constructor(amount: Double) : this(amount.toInternalBigDecimal())

    operator fun plus(other: MonetaryAmount) = copy(amount = amount + other.amount)

    operator fun minus(other: MonetaryAmount) = copy(amount = amount - other.amount)

    operator fun times(multiplicator: Byte) = times(multiplicator.toLong())
    operator fun times(multiplicator: Short) = times(multiplicator.toLong())
    operator fun times(multiplicator: Int) = times(multiplicator.toLong())
    operator fun times(multiplicator: Long) = copy(amount = amount * multiplicator.toInternalBigDecimal())
    operator fun times(multiplicator: Float) = times(multiplicator.toDouble())
    operator fun times(multiplicator: Double) = copy(amount = amount * multiplicator.toInternalBigDecimal())
    operator fun times(multiplicator: BigInteger) = copy(amount = amount * multiplicator.toInternalBigDecimal())
    operator fun times(multiplicator: BigDecimal) = copy(amount = amount * multiplicator)

    operator fun div(divisor: Byte) = div(divisor.toLong())
    operator fun div(divisor: Short) = div(divisor.toLong())
    operator fun div(divisor: Int) = div(divisor.toLong())
    operator fun div(divisor: Long) = copy(amount = amount / divisor.toInternalBigDecimal())
    operator fun div(divisor: Float) = div(divisor.toDouble())
    operator fun div(divisor: Double) = copy(amount = amount / divisor.toInternalBigDecimal())
    operator fun div(multiplicator: BigInteger) = copy(amount = amount / multiplicator.toInternalBigDecimal())
    operator fun div(multiplicator: BigDecimal) = copy(amount = amount / multiplicator)

    operator fun div(divisor: MonetaryAmount): BigDecimal = amount / divisor.amount

    fun round(scale: Int, roundingMode: RoundingMode): MonetaryAmount =
        if (roundingMode == CEILING || (roundingMode == UP && amount > ZERO) || (roundingMode == DOWN && amount < ZERO)) {
            (this - NEGLIGIBLE_ERROR).roundImpl(scale, roundingMode)
        } else if (roundingMode == FLOOR || (roundingMode == DOWN && amount > ZERO) || (roundingMode == UP && amount < ZERO)) {
            (this + NEGLIGIBLE_ERROR).roundImpl(scale, roundingMode)
        } else {
            roundImpl(scale, roundingMode)
        }

    fun roundUp(scale: Int): MonetaryAmount = round(scale, UP)

    private fun roundImpl(scale: Int, roundingMode: RoundingMode) =
        copy(amount = amount.setScale(scale, roundingMode))

    fun integerPart(): BigDecimal = round(0, DOWN).amount
    fun fractionPart(): BigDecimal = ((amount - integerPart()).abs() * 10.0.pow(amount.scale()).toInternalBigDecimal()).setScale(0)

    fun format(locale: Locale): String =
        NumberFormat.getCurrencyInstance(locale).format(amount)

    override fun compareTo(other: MonetaryAmount): Int = COMPARATOR.compare(this, other)

    companion object {
        private val COMPARATOR = compareBy<MonetaryAmount> { it.amount }
        private val NEGLIGIBLE_ERROR = MonetaryAmount(ONE) / 10.0.pow(5)
    }
}
