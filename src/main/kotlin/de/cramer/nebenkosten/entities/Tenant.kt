package de.cramer.nebenkosten.entities

import javax.persistence.*

@Entity
@Table(name = "tenants")
data class Tenant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long,

    @Column(name = "first_name")
    val firstName: String,
    @Column(name = "last_name")
    val lastName: String,

    @Embedded
    val address: Address,

    @Column(name = "hidden")
    val hidden: Boolean
) : Comparable<Tenant> {
    constructor(firstName: String, lastName: String, address: Address, hidden: Boolean) : this(0, firstName, lastName, address, hidden)

    override fun compareTo(other: Tenant): Int = COMPARATOR.compare(this, other)

    val name: String
        get() = "$firstName $lastName"

    override fun toString(): String = name

    companion object {

        private val COMPARATOR = compareBy<Tenant> { it.lastName }
            .thenBy { it.firstName }
            .thenBy { it.address }
    }
}
