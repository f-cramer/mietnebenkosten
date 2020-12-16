package de.cramer.nebenkosten.entities

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "rental")
data class Rental(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "flat")
    val flat: Flat,

    @ManyToOne
    @JoinColumn(name = "tenant")
    val tenant: Tenant,

    @Column(name = "persons")
    val persons: Int,

    @Embedded
    val period: LocalDatePeriod
) : Comparable<Rental> {
    constructor(flat: Flat, tenant: Tenant, persons: Int, period: LocalDatePeriod) : this(0, flat, tenant, persons, period)

    override fun compareTo(other: Rental) = COMPARATOR.compare(this, other)

    override fun toString(): String = "$flat - $tenant ($period)"

    companion object {

        private val COMPARATOR = Comparator.comparing<Rental, Flat> { it.flat }
            .thenComparing<LocalDatePeriod> { it.period }
            .thenComparing<Tenant> { it.tenant }
            .thenComparingInt { it.persons }
    }
}
