package de.cramer.nebenkosten.services

import de.cramer.nebenkosten.entities.User
import de.cramer.nebenkosten.repositories.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val repository: UserRepository,
) {

    fun saveUser(user: User) {
        repository.save(user)
    }

    fun getUser(username: String): User? {
        return repository.findById(username).orElse(null)
    }
}
