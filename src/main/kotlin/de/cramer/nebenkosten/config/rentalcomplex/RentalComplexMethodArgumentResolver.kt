package de.cramer.nebenkosten.config.rentalcomplex

import de.cramer.nebenkosten.entities.RentalComplex
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class RentalComplexMethodArgumentResolver(
    private val rentalComplexResolver: RentalComplexResolver,
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.parameterType == RentalComplex::class.java

    override fun resolveArgument(parameter: MethodParameter, mavContainer: ModelAndViewContainer?, webRequest: NativeWebRequest, binderFactory: WebDataBinderFactory?): RentalComplex? {
        return rentalComplexResolver.getCurrentRentalComplex(webRequest.nativeRequest as HttpServletRequest)
    }
}
