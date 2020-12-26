package de.cramer.nebenkosten.entities

import org.springframework.context.MessageSource
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.TemporalAdjusters.*
import java.util.*
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
data class LocalDatePeriod(
    @Column(name = "start")
    val start: LocalDate,
    @Basic(optional = true)
    @Column(name = "end", nullable = true)
    val end: LocalDate? = null
) : Comparable<LocalDatePeriod> {
    init {
        require(end == null || !start.isAfter(end)) { "start cannot be after end ($start > $end)" }
    }

    fun isOverlapping(other: LocalDatePeriod): Boolean {
        val oEnd = other.end
        if (end == null && oEnd == null) {
            return true
        }

        val oStart = other.start

        return when {
            end == null -> oEnd!! > start
            oEnd == null -> end > oStart
            oStart <= start -> start < oEnd
            start <= oStart -> oStart < end
            oStart < end -> end <= oEnd
            start < oEnd -> oEnd <= end
            else -> false
        }
    }

    fun intersect(other: LocalDatePeriod): LocalDatePeriod {
        if (!isOverlapping(other)) {
            throw IllegalArgumentException()
        }

        val start = maxOf(start, other.start)
        val end = if (end != null && other.end != null) {
            minOf(end, other.end)
        } else if (end != null && other.end == null) {
            end
        } else if (end == null && other.end != null) {
            other.end
        } else {
            null
        }
        return LocalDatePeriod(start, end)
    }

    override fun compareTo(other: LocalDatePeriod): Int = COMPARATOR.compare(this, other)

    override fun toString(): String = if(end == null) {
        "$start - open"
    } else {
        "$start - $end"
    }

    fun format(messageSource: MessageSource, locale: Locale): String {
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
        val startString = formatter.format(start)
        return if (end != null) {
            val endString = formatter.format(end)
            messageSource.getMessage("LocalDatePeriod.closed", arrayOf(startString, endString), locale)
        } else {
            messageSource.getMessage("LocalDatePeriod.open", arrayOf(startString), locale)
        }
    }

    companion object {

        private val COMPARATOR = compareBy<LocalDatePeriod> { it.start }
            .thenBy(nullsLast()) { it.end }

        fun ofYear(year: Year): LocalDatePeriod {
            val someDay = year.atDay(1)
            val start = someDay.with(firstDayOfYear())
            val end = someDay.with(lastDayOfYear())
            return LocalDatePeriod(start, end)
        }

        fun ofMonth(month: YearMonth): LocalDatePeriod {
            val someDay = month.atDay(1)
            val start = someDay.with(firstDayOfMonth())
            val end = someDay.with(lastDayOfMonth())
            return LocalDatePeriod(start, end)
        }
    }
}
