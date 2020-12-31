package de.cramer.nebenkosten.entities

import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
data class Address(
    @Column(name = "street")
    val street: String,

    @Column(name = "house_number")
    val houseNumber: Int,

    @Column(name = "zip_code")
    val zipCode: String,

    @Column(name = "city")
    val city: String,

    @Column(name = "country")
    val country: String? = null
) : Comparable<Address> {
    override fun compareTo(other: Address): Int = COMPARATOR.compare(this, other)

    companion object {

        private val COMPARATOR = compareBy<Address, String?>(nullsFirst(String.CASE_INSENSITIVE_ORDER)) { it.country }
            .thenBy { it.zipCode }
            .thenBy { it.city }
            .thenBy { it.street }
            .thenBy { it.houseNumber }
    }
}
