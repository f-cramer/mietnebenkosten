package de.cramer.nebenkosten.security

import de.cramer.nebenkosten.services.UserService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsPasswordService
import org.springframework.stereotype.Service

@Service
class SecurityUserPasswordService(
    private val userService: UserService,
) : UserDetailsPasswordService {

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
