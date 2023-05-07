package de.cramer.nebenkosten.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table

@Entity
@Table(name = "rental_complexes")
data class RentalComplex(
    @Id
    @Column(name = "rental_complex_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(name = "name")
    var name: String,

    @ManyToMany
    @JoinTable(
        name = "users_rental_complexes",
        joinColumns = [JoinColumn(name = "rental_complex_id")],
        inverseJoinColumns = [JoinColumn(name = "username")],
    )
    val users: MutableList<User> = mutableListOf(),
) : Comparable<RentalComplex> {
    override fun compareTo(other: RentalComplex): Int = COMPARATOR.compare(this, other)

    override fun toString(): String = name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RentalComplex

        if (id != other.id) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }

    companion object {

        private val COMPARATOR = compareBy<RentalComplex> { it.name }
            .thenBy { it.id }
    }
}
