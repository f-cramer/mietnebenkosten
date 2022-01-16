package de.cramer.nebenkosten.entities

import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "landlords")
class Landlord(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long,

    @Column(name = "first_name")
    val firstName: String,
    @Column(name = "last_name")
    val lastName: String,

    val iban: String,

    @Embedded
    val address: Address,

    @Embedded
    val period: YearPeriod,
) : Comparable<Landlord> {
    override fun compareTo(other: Landlord): Int = COMPARATOR.compare(this, other)

    val name: String
        get() = "$firstName $lastName"

    override fun toString(): String = name

    companion object {

        private val COMPARATOR = compareBy<Landlord> { it.period }
            .thenBy { it.lastName }
            .thenBy { it.firstName }
            .thenBy { it.address }
    }
}
