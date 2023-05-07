package de.cramer.nebenkosten.config

import de.cramer.nebenkosten.config.rentalcomplex.RentalComplexResolver
import de.cramer.nebenkosten.security.SecurityUser
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder

@Configuration
class SecurityConfiguration(
    private val userDetailsService: UserDetailsService,
) {

    @Bean
    fun configure(http: HttpSecurity): SecurityFilterChain {
        http.httpBasic {
            it.disable()
        }

        http.authorizeHttpRequests {
            it.requestMatchers(AntPathRequestMatcher("/css/**")).permitAll()
            it.requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
            it.anyRequest().authenticated()
        }

        http.formLogin {
            it.loginPage("/login")
            it.permitAll()
        }

        http.rememberMe {}

        http.userDetailsService(userDetailsService)

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

    @EventListener
    fun onLoginSuccess(event: InteractiveAuthenticationSuccessEvent) {
        val user = event.authentication.principal as? SecurityUser ?: return
        val attributes = RequestContextHolder.getRequestAttributes() ?: return
        if (attributes.getAttribute(RentalComplexResolver.ATTRIBUTE_NAME, RequestAttributes.SCOPE_SESSION) != null) {
            return
        }

        val rentalComplex = user.user.rentalComplexes.firstOrNull() ?: return
        attributes.setAttribute(RentalComplexResolver.ATTRIBUTE_NAME, rentalComplex.id, RequestAttributes.SCOPE_SESSION)
    }
}
