package de.cramer.nebenkosten.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class User(
    @Id
    @Column(name = "username")
    val username: String,
    @Column(name = "password")
    var password: String,

    @ManyToMany
    @JoinTable(
        name = "users_rental_complexes",
        joinColumns = [JoinColumn(name = "username")],
        inverseJoinColumns = [JoinColumn(name = "rental_complex_id")],
    )
    val rentalComplexes: MutableList<RentalComplex> = mutableListOf(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (username != other.username) return false
        if (password != other.password) return false

        return true
    }

    override fun hashCode(): Int {
        var result = username.hashCode()
        result = 31 * result + password.hashCode()
        return result
    }
}
