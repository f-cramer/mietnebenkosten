package de.cramer.nebenkosten.config

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

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
            it.requestMatchers("/css/**").permitAll()
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
}
