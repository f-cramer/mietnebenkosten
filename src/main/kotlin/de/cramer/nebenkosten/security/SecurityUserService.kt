package de.cramer.nebenkosten.security

import de.cramer.nebenkosten.services.UserService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsPasswordService
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class SecurityUserService(
    private val userService: UserService,
) : UserDetailsService, UserDetailsPasswordService {

    override fun loadUserByUsername(username: String): UserDetails =
        userService.getUser(username)?.let { SecurityUser(it) } ?: throw UsernameNotFoundException(username)

    override fun updatePassword(user: UserDetails, newPassword: String): UserDetails {
        if (user is SecurityUser) {
            user.user.also {
                it.password = newPassword
                userService.saveUser(it)

                return SecurityUser(userService.getUser(it.username)!!)
            }
        }

        return user
    }
}
