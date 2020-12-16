package de.cramer.nebenkosten.entities

import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
data class Address(
    @Column(name = "street")
    val street: String,

    @Column(name = "house_numer")
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

        private val COMPARATOR = Comparator.comparing<Address, String>({ it.country }, Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER))
            .thenComparing<String> { it.zipCode }
            .thenComparing<String> { it.city }
            .thenComparing<String> { it.street }
            .thenComparingInt { it.houseNumber }
    }
}
