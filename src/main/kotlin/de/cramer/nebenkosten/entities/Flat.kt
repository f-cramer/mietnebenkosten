package de.cramer.nebenkosten.entities

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "flats")
data class Flat(
    @Id
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
