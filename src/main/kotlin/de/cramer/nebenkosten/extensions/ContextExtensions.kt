package de.cramer.nebenkosten.extensions

import de.cramer.nebenkosten.entities.User
import de.cramer.nebenkosten.security.SecurityUser
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

val currentUser: User?
    get() {
        val principal = SecurityContextHolder.getContext().authentication?.principal as? SecurityUser ?: return null
        return principal.user
    }

val currentRequest: HttpServletRequest?
    get() {
        val attributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes ?: return null
        return attributes.request
    }
