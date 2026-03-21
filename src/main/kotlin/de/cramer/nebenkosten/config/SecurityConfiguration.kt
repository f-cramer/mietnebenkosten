package de.cramer.nebenkosten.config

import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher

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
}
