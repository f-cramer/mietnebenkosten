package de.cramer.nebenkosten.entities

import javax.persistence.*

@Entity
@Table(name = "rentals")
data class Rental(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "flat_name")
    val flat: Flat,

    @ManyToOne
    @JoinColumn(name = "tenant_id")
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

        private val COMPARATOR = compareBy<Rental> { it.flat }
            .thenBy { it.period }
            .thenBy { it.tenant }
            .thenBy { it.persons }
    }
}
