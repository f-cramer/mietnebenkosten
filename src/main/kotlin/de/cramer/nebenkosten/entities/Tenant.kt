package de.cramer.nebenkosten.entities

import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

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

    @ManyToOne(optional = false)
    @JoinColumn(name = "rental_complex_id", nullable = false)
    val rentalComplex: RentalComplex,

    @Column(name = "hidden")
    val hidden: Boolean,

    @Transient
    val generated: Boolean = false,
) : Comparable<Tenant> {
    constructor(firstName: String, lastName: String, address: Address, gender: Gender, formOfAddress: FormOfAddress, rentalComplex: RentalComplex, hidden: Boolean, generated: Boolean = false) : this(0, firstName, lastName, address, gender, formOfAddress, rentalComplex, hidden, generated)

    override fun compareTo(other: Tenant): Int = COMPARATOR.compare(this, other)

    val name: String
        get() = "$firstName $lastName"

    override fun toString(): String = name

    companion object {

        private val COMPARATOR = compareBy<Tenant> { it.generated }
            .thenBy { it.lastName }
            .thenBy { it.firstName }
            .thenBy { it.address }
    }
}
