package de.cramer.nebenkosten.entities

import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "contracts")
data class Contract(
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
    val period: LocalDatePeriod,
) : Comparable<Contract> {
    constructor(flat: Flat, tenant: Tenant, persons: Int, period: LocalDatePeriod) : this(0, flat, tenant, persons, period)

    override fun compareTo(other: Contract) = COMPARATOR.compare(this, other)

    override fun toString(): String = "$flat - $tenant ($period)"

    companion object {

        private val COMPARATOR = compareBy<Contract> { it.flat }
            .thenBy { it.period }
            .thenBy { it.tenant }
            .thenBy { it.persons }
    }
}
