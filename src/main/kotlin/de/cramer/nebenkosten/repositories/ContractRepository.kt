package de.cramer.nebenkosten.repositories

import de.cramer.nebenkosten.entities.Contract
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface ContractRepository : JpaRepository<Contract, Long>, JpaSpecificationExecutor<Contract>
