package de.cramer.nebenkosten.extensions

import javax.persistence.criteria.Path
import de.cramer.nebenkosten.entities.LocalDatePeriod
import de.cramer.nebenkosten.entities.LocalDatePeriod_
import org.springframework.data.jpa.domain.Specification

inline fun <T> overlappingDatePeriodSpecification(
    period: LocalDatePeriod,
    crossinline selector: (Path<T>) -> Path<LocalDatePeriod>,
): Specification<T> = Specification { root, _, criteriaBuilder ->
    val start = period.start
    val end = period.end

    val periodPath = selector(root)

    val dbStart = periodPath.get(LocalDatePeriod_.start)
    val dbEnd = periodPath.get(LocalDatePeriod_.end)

    criteriaBuilder.run {
        if (end == null) {
            or(
                isNull(dbEnd),
                greaterThanOrEqualTo(dbEnd, start)
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
}
