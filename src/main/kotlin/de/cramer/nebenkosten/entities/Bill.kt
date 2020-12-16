package de.cramer.nebenkosten.entities

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "bills")
data class Bill(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long,

    @Column(name = "description")
    val description: String,

    @Embedded
    val period: LocalDatePeriod,

    @Embedded
    val price: MonetaryAmount
) : Comparable<Bill> {
    constructor(description: String, period: LocalDatePeriod, price: MonetaryAmount) : this(0, description, period, price)

    override fun compareTo(other: Bill): Int = COMPARATOR.compare(this, other)

    companion object {

        private val COMPARATOR = Comparator.comparing<Bill, LocalDatePeriod> { it.period }
            .thenComparing<MonetaryAmount> { it.price }
    }
}
