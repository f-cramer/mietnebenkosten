package de.cramer.nebenkosten.entities

import javax.persistence.*

@Entity
@Table(name = "invoices")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
sealed class Invoice(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long,

    @Column(name = "description")
    val description: String,

    @Embedded
    val period: LocalDatePeriod,

    @Embedded
    val price: MonetaryAmount,

    @Column(name = "order")
    val order: Int
) : Comparable<Invoice> {

    abstract val type: InvoiceType

    override fun compareTo(other: Invoice): Int = COMPARATOR.compare(this, other)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Invoice

        if (id != other.id) return false
        if (description != other.description) return false
        if (period != other.period) return false
        if (price != other.price) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + period.hashCode()
        result = 31 * result + price.hashCode()
        return result
    }

    companion object {

        private val COMPARATOR = compareBy<Invoice> { it.order }
            .thenBy { it.description }
            .thenBy { it.period }
            .thenBy { it.price }
    }
}

@Entity
@DiscriminatorValue("general")
class GeneralInvoice(
    id: Long,
    description: String,
    period: LocalDatePeriod,
    price: MonetaryAmount,
    order: Int,

    @Suppress("JpaAttributeTypeInspection")
    @Column(name = "split_algorithm")
    val splitAlgorithm: SplitAlgorithm
) : Invoice(id, description, period, price, order) {

    override val type: InvoiceType
        get() = InvoiceType.General

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as GeneralInvoice

        if (splitAlgorithm != other.splitAlgorithm) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + splitAlgorithm.hashCode()
        return result
    }
}

@Entity
@DiscriminatorValue("rental")
class RentalInvoice(
    id: Long,
    description: String,
    period: LocalDatePeriod,
    price: MonetaryAmount,
    order: Int,

    @ManyToOne
    @JoinColumn(name = "rental_id")
    val rental: Rental
) : Invoice(id, description, period, price, order) {

    override val type: InvoiceType
        get() = InvoiceType.Rental

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as RentalInvoice

        if (rental != other.rental) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + rental.hashCode()
        return result
    }
}

enum class InvoiceType {
    General, Rental
}
