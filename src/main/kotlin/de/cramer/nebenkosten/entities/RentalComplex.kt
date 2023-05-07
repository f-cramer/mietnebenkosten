package de.cramer.nebenkosten.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "rental_complexes")
data class RentalComplex(
    @Id
    @Column(name = "rental_complex_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(name = "name")
    val name: String,
) : Comparable<RentalComplex> {
    override fun compareTo(other: RentalComplex): Int = COMPARATOR.compare(this, other)

    override fun toString(): String = name

    companion object {

        private val COMPARATOR = compareBy<RentalComplex> { it.name }
            .thenBy { it.id }
    }
}
