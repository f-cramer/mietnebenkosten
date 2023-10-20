package de.cramer.nebenkosten.extensions

import de.cramer.nebenkosten.entities.LocalDatePeriod
import de.cramer.nebenkosten.entities.LocalDatePeriod_
import de.cramer.nebenkosten.entities.YearPeriod
import de.cramer.nebenkosten.entities.YearPeriod_
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification

inline fun <T> overlappingDatePeriodSpecification(
    period: LocalDatePeriod,
    crossinline selector: (Path<T>) -> Path<LocalDatePeriod>,
): Specification<T> = Specification { root, _, criteriaBuilder ->
    val periodPath = selector(root)
    val dbStart = periodPath.get(LocalDatePeriod_.start)
    val dbEnd = periodPath.get(LocalDatePeriod_.end)

    criteriaBuilder.overlappingPeriodSpecification(period.start, period.end, dbStart, dbEnd)
}

inline fun <T> overlappingYearPeriodSpecification(
    period: YearPeriod,
    crossinline selector: (Path<T>) -> Path<YearPeriod>,
): Specification<T> = Specification { root, _, criteriaBuilder ->
    val periodPath = selector(root)
    val dbStart = periodPath.get(YearPeriod_.start)
    val dbEnd = periodPath.get(YearPeriod_.end)

    criteriaBuilder.overlappingPeriodSpecification(period.start, period.end, dbStart, dbEnd)
}

fun <T : Comparable<T>> CriteriaBuilder.overlappingPeriodSpecification(
    start: T,
    end: T?,
    dbStart: Path<T>,
    dbEnd: Path<T>,
): Predicate {
    return if (end == null) {
        or(
            isNull(dbEnd),
            greaterThanOrEqualTo(dbEnd, start),
        )
    } else {
        or(
            and(isNull(dbEnd), greaterThanOrEqualTo(end, dbStart)),
            and(lessThanOrEqualTo(dbStart, start), lessThanOrEqualTo(start, dbEnd)),
            and(lessThanOrEqualTo(start, dbStart), lessThanOrEqualTo(dbStart, end)),
            and(lessThanOrEqualTo(dbStart, end), lessThanOrEqualTo(end, dbEnd)),
            and(lessThanOrEqualTo(start, dbEnd), lessThanOrEqualTo(dbEnd, end)),
        )
    }
}
