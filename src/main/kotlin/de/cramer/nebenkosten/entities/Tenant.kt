package de.cramer.nebenkosten.entities

import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

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

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    val gender: Gender,

    @Enumerated(EnumType.STRING)
    @Column(name = "form_of_address")
    val formOfAddress: FormOfAddress,

    @Column(name = "hidden")
    val hidden: Boolean,
) : Comparable<Tenant> {
    constructor(firstName: String, lastName: String, address: Address, gender: Gender, formOfAddress: FormOfAddress, hidden: Boolean) : this(0, firstName, lastName, address, gender, formOfAddress, hidden)

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
