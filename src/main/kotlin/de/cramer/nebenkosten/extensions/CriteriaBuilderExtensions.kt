package de.cramer.nebenkosten.extensions

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Predicate

fun <X : Comparable<X>> CriteriaBuilder.lessThanOrEqualTo(x: X, y: Expression<out X>): Predicate =
    greaterThanOrEqualTo(y, x)

fun <X : Comparable<X>> CriteriaBuilder.greaterThanOrEqualTo(x: X, y: Expression<out X>): Predicate =
    lessThanOrEqualTo(y, x)

fun CriteriaBuilder.le(x: Number?, y: Expression<out Number?>?): Predicate =
    ge(y, x)

fun CriteriaBuilder.ge(x: Number?, y: Expression<out Number?>?): Predicate =
    le(y, x)
