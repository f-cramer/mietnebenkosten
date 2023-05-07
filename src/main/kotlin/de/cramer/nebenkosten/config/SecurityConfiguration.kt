package de.cramer.nebenkosten.config

import de.cramer.nebenkosten.config.rentalcomplex.RentalComplexResolver
import de.cramer.nebenkosten.security.SecurityUser
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder

@Configuration
class SecurityConfiguration(
    private val userDetailsService: UserDetailsService,
) {

    @Bean
    fun configure(http: HttpSecurity): SecurityFilterChain = http
        .httpBasic {
            it.disable()
        }
        .authorizeHttpRequests {
            it.requestMatchers(
                PathPatternRequestMatcher.withDefaults().matcher("/css/**"),
                EndpointRequest.toAnyEndpoint(),
            ).permitAll()
            it.anyRequest().authenticated()
        }
        .formLogin {
            it.loginPage("/login")
            it.permitAll()
        }
        .rememberMe {}
        .userDetailsService(userDetailsService)
        .build()

    @Bean
    fun passwordEncoder(): PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

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
