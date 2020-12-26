package de.cramer.nebenkosten.utils

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext

private val MATH_CONTEXT = MathContext.DECIMAL128
private val SCALE = MATH_CONTEXT.precision

val ZERO: BigDecimal = BigDecimal.ZERO.ensureScale()
val ONE: BigDecimal = BigDecimal.ONE.ensureScale()

private fun BigDecimal.ensureScale(): BigDecimal = if (scale() < SCALE) setScale(SCALE) else this
fun Byte.toInternalBigDecimal(): BigDecimal = toInt().toInternalBigDecimal()
fun Short.toInternalBigDecimal(): BigDecimal = toInt().toInternalBigDecimal()
fun Int.toInternalBigDecimal(): BigDecimal = toBigDecimal(MATH_CONTEXT).ensureScale()
fun Long.toInternalBigDecimal(): BigDecimal = toBigDecimal(MATH_CONTEXT).ensureScale()
fun Float.toInternalBigDecimal(): BigDecimal = toBigDecimal(MATH_CONTEXT).ensureScale()
fun Double.toInternalBigDecimal(): BigDecimal = toBigDecimal(MATH_CONTEXT).ensureScale()
fun BigInteger.toInternalBigDecimal(): BigDecimal = toBigDecimal(SCALE, MATH_CONTEXT)
fun BigDecimal.toInternalBigDecimal(): BigDecimal = ensureScale()
