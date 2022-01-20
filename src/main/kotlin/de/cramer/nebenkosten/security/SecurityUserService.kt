package de.cramer.nebenkosten.security

import de.cramer.nebenkosten.services.UserService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class SecurityUserService(
    private val userService: UserService,
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails =
        userService.getUser(username)?.let { SecurityUser(it) } ?: throw UsernameNotFoundException(username)
}
