package de.cramer.nebenkosten.repositories

import de.cramer.nebenkosten.entities.Invoice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface InvoiceRepository : JpaRepository<Invoice, Long>, JpaSpecificationExecutor<Invoice>
