package de.cramer.nebenkosten.config

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.security.crypto.password.DelegatingPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class SecurityConfiguration(
    private val userDetailsService: UserDetailsService,
) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http.httpBasic().disable()

        http.authorizeRequests()
            .antMatchers("/css/**").permitAll()
            .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
            .anyRequest().authenticated()

        http.formLogin().apply {
            loginPage("/login")
            permitAll()
        }

        http.rememberMe()
    }

    override fun userDetailsService(): UserDetailsService = userDetailsService

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return DelegatingPasswordEncoder(
            "argon2",
            mapOf("argon2" to Argon2PasswordEncoder())
        ).apply {
            @Suppress("DEPRECATION", "RemoveRedundantQualifierName")
            setDefaultPasswordEncoderForMatches(org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance())
        }
    }
}