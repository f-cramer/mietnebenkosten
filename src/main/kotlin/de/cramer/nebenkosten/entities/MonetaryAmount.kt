package de.cramer.nebenkosten.entities

import java.util.*
import javax.persistence.Column
import javax.persistence.Embeddable
import kotlin.math.roundToLong

@Embeddable
data class MonetaryAmount(
    @Column(name = "monetary_amount")
    val amount: Long
) : Comparable<MonetaryAmount> {
    operator fun plus(other: MonetaryAmount) = MonetaryAmount(amount + other.amount)

    operator fun minus(other: MonetaryAmount) = MonetaryAmount(amount - other.amount)

    operator fun times(muliplicator: Byte) = MonetaryAmount(amount * muliplicator)
    operator fun times(muliplicator: Short) = MonetaryAmount(amount * muliplicator)
    operator fun times(muliplicator: Int) = MonetaryAmount(amount * muliplicator)
    operator fun times(muliplicator: Long) = MonetaryAmount(amount * muliplicator)
    operator fun times(muliplicator: Float) = MonetaryAmount(((amount * muliplicator).roundToLong()))
    operator fun times(muliplicator: Double) = MonetaryAmount((amount * muliplicator).roundToLong())

    operator fun div(divisor: Byte) = MonetaryAmount((amount.toDouble() / divisor).roundToLong())
    operator fun div(divisor: Short) = MonetaryAmount((amount.toDouble() / divisor).roundToLong())
    operator fun div(divisor: Int) = MonetaryAmount((amount.toDouble() / divisor).roundToLong())
    operator fun div(divisor: Long) = MonetaryAmount((amount.toDouble() / divisor).roundToLong())
    operator fun div(divisor: Float) = MonetaryAmount((amount.toDouble() / divisor).roundToLong())
    operator fun div(divisor: Double) = MonetaryAmount((amount.toDouble() / divisor).roundToLong())

    operator fun div(divisor: MonetaryAmount): Double = amount.toDouble() / divisor.amount

    override fun compareTo(other: MonetaryAmount): Int = COMPARATOR.compare(this, other)

    companion object {
        private val COMPARATOR = Comparator.comparingLong<MonetaryAmount> { it.amount }
    }
}
