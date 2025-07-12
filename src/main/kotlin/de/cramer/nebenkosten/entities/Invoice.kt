package de.cramer.nebenkosten.entities

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "invoices")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
abstract class Invoice(
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
    val order: Int,
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
            .thenBy(nullsFirst<Contract>()) { if (it is ContractInvoice) it.contract else null }
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
    val splitAlgorithm: SplitAlgorithm,
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
@DiscriminatorValue("contract")
class ContractInvoice(
    id: Long,
    description: String,
    period: LocalDatePeriod,
    price: MonetaryAmount,
    order: Int,

    @ManyToOne
    @JoinColumn(name = "contract_id")
    val contract: Contract,
) : Invoice(id, description, period, price, order) {

    override val type: InvoiceType
        get() = InvoiceType.Contract

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ContractInvoice

        if (contract != other.contract) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + contract.hashCode()
        return result
    }
}

enum class InvoiceType {
    General,
    Contract,
}
