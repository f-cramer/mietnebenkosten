package de.cramer.nebenkosten.aot

import org.springframework.aot.hint.ExecutableMode
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import java.util.Locale

class ThymeleafRuntimeHints : RuntimeHintsRegistrar {
    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {
        hints.reflection()
            .registerMethod(Locale::class.java.getDeclaredMethod("getLanguage"), ExecutableMode.INVOKE)

        hints.resources()
            .registerPattern("*.properties")
    }
}
