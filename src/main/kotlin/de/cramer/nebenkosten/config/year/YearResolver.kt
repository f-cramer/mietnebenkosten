package de.cramer.nebenkosten.config.year

import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.util.WebUtils
import java.time.Year
import javax.servlet.http.HttpServletRequest
import kotlin.reflect.jvm.jvmName

@Component
class YearResolver: HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.parameterType == Year::class.java

    override fun resolveArgument(parameter: MethodParameter, mavContainer: ModelAndViewContainer?, webRequest: NativeWebRequest, binderFactory: WebDataBinderFactory?): Year =
        WebUtils.getSessionAttribute(webRequest.nativeRequest as HttpServletRequest, ATTRIBUTE_NAME) as? Year ?: Year.now()

    companion object {
        val ATTRIBUTE_NAME: String = "${YearResolver::class.jvmName}.year"
    }
}
