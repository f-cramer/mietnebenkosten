package de.cramer.nebenkosten.config

import de.cramer.nebenkosten.config.year.YearChangeInterceptor
import de.cramer.nebenkosten.config.year.YearResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfiguration(
    private val yearResolver: YearResolver,
    private val yearChangeInterceptor: YearChangeInterceptor,
) : WebMvcConfigurer {

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers += yearResolver
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(yearChangeInterceptor)
    }
}
