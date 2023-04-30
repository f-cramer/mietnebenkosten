package de.cramer.nebenkosten.aot

import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.classreading.MetadataReader
import java.util.Calendar

class JasperreportsRuntimeHints : RuntimeHintsRegistrar {

    private val resources = setOf(
        "default.jasperreports.properties",
        "jasperreports_extension.properties",
        "net/sf/jasperreports/components/components.xsd",
        "net/sf/jasperreports/components/messages.properties",
        "net/sf/jasperreports/engine/dtds/jasperprint.xsd",
        "net/sf/jasperreports/engine/dtds/jasperprint-dtd-compat.xsd",
        "net/sf/jasperreports/engine/dtds/jasperreport.xsd",
        "net/sf/jasperreports/engine/dtds/jasperreport-dtd-compat.xsd",
        "net/sf/jasperreports/engine/dtds/jaspertemplate.xsd",
        "net/sf/jasperreports/engine/dtds/jaspertemplate-dtd-compat.xsd",
        "net/sf/jasperreports/parts/parts.xsd",
        "jasperreports_messages.properties",
        "metadata_messages.properties",
        "metadata_messages-defaults.properties",
        "properties-metadata.json",
        "jasperreports.properties",
        "reports/*",
    )

    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {
        hints.resources().apply {
            resources.forEach { registerPattern(it) }
        }

        hints.reflection().apply {
            val scanner = JasperreportsClasspathScanner()
            scanner.findCandidateComponents("net.sf.jasperreports").asSequence()
                .mapNotNull { it.beanClassName }
                .forEach { registerTypeIfPresent(null, it, *MemberCategory.entries.toTypedArray()) }
        }

        hints.serialization().apply {
            registerType(Array<Calendar>::class.java)
        }
    }

    private class JasperreportsClasspathScanner : ClassPathScanningCandidateComponentProvider(false) {
        override fun isCandidateComponent(beanDefinition: AnnotatedBeanDefinition): Boolean {
            return true
        }

        override fun isCandidateComponent(metadataReader: MetadataReader): Boolean {
            return true
        }
    }
}
