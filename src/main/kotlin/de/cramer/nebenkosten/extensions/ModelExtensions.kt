package de.cramer.nebenkosten.extensions

import org.springframework.ui.Model

operator fun Model.set(attributeName: String, attributeValue: Any?) {
    this.addAttribute(attributeName, attributeValue)
}
