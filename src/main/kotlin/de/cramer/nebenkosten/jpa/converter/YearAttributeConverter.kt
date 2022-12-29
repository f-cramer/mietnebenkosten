package de.cramer.nebenkosten.jpa.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.springframework.stereotype.Component
import java.time.Year

@Component
@Converter(autoApply = true)
class YearAttributeConverter : AttributeConverter<Year?, Short?> {
    override fun convertToDatabaseColumn(attribute: Year?): Short? {
        return attribute?.value?.toShort()
    }

    override fun convertToEntityAttribute(dbData: Short?): Year? {
        return if (dbData != null) Year.of(dbData.toInt()) else null
    }
}
