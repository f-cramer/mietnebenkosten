package de.cramer.nebenkosten.repositories

import de.cramer.nebenkosten.entities.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, String>
