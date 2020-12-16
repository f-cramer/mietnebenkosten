package de.cramer.nebenkosten.utils

import de.cramer.nebenkosten.entities.LocalDatePeriod
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlin.math.ceil
import kotlin.math.max

object LocalDatePeriodUtils {

    fun LocalDatePeriod.getLengthInMonths(): Double {
        val end = this.end ?: LocalDate.now()

        val startYearMonth = YearMonth.from(start)
        val endYearMonth = YearMonth.from(end)

        if (startYearMonth == endYearMonth) {
            return ((start.until(end, ChronoUnit.DAYS) + 1) / 30).roundToHalf()
        }

        val (firstFullMonth, additionalDaysFromStart) = if (start == start.with(TemporalAdjusters.firstDayOfMonth())) {
            Pair(startYearMonth, 0L)
        } else {
            val nextMonth = startYearMonth.plusMonths(1)
            Pair(nextMonth, start.until(nextMonth.atDay(1), ChronoUnit.DAYS))
        }

        val (lastFullMonth, additionalDaysTilEnd) = if (end == end.with(TemporalAdjusters.lastDayOfMonth())) {
            Pair(endYearMonth, 0L)
        } else {
            val previousMonth = endYearMonth.minusMonths(1)
            Pair(previousMonth, end.dayOfMonth.toLong())
        }

        val months = max(0, firstFullMonth.until(lastFullMonth, ChronoUnit.MONTHS) + 1)
        return months + (((additionalDaysFromStart + additionalDaysTilEnd) / 30.toDouble())).roundToHalf()
    }

    private fun Number.roundToHalf(): Double = ceil(this.toDouble() * 2) / 2.toDouble()
}
