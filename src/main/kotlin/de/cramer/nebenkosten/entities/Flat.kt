package de.cramer.nebenkosten.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "flats")
data class Flat(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flat_id")
    val id: Long,
    @Column(name = "name")
    val name: String,
    @Column(name = "area")
    val area: Long,
    @Column(name = "order")
    val order: Int = 0,
) : Comparable<Flat> {
    override fun compareTo(other: Flat): Int = COMPARATOR.compare(this, other)

    override fun toString(): String = name

    companion object {

        private val COMPARATOR = compareBy<Flat> { it.order }
            .thenBy { it.name }
            .thenBy { it.area }
    }
}
