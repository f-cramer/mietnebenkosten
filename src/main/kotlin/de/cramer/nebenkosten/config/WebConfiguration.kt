package de.cramer.nebenkosten.config

import de.cramer.nebenkosten.config.rentalcomplex.RentalComplexChangeInterceptor
import de.cramer.nebenkosten.config.rentalcomplex.RentalComplexMethodArgumentResolver
import de.cramer.nebenkosten.config.year.YearChangeInterceptor
import de.cramer.nebenkosten.config.year.YearResolver
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor
import org.springframework.web.servlet.i18n.SessionLocaleResolver

@Configuration
class WebConfiguration(
    private val yearResolver: YearResolver,
    private val yearChangeInterceptor: YearChangeInterceptor,
    private val rentalComplexResolver: RentalComplexMethodArgumentResolver,
    private val rentalComplexChangeInterceptor: RentalComplexChangeInterceptor,
) : WebMvcConfigurer {

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers += yearResolver
        resolvers += rentalComplexResolver
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(yearChangeInterceptor)
        registry.addInterceptor(rentalComplexChangeInterceptor)
        registry.addInterceptor(LocaleChangeInterceptor())
    }

    @Bean
    fun localeResolver(): LocaleResolver = SessionLocaleResolver()

    @Bean
    fun openEntityManagerInViewFilter(): FilterRegistrationBean<OpenEntityManagerInViewFilter> = FilterRegistrationBean<OpenEntityManagerInViewFilter>().apply {
        filter = OpenEntityManagerInViewFilter()
        order = Ordered.HIGHEST_PRECEDENCE + 2
    }
}
