package de.cramer.nebenkosten.jpa.converter

import com.fasterxml.jackson.databind.ObjectMapper
import de.cramer.nebenkosten.entities.SplitAlgorithm
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.springframework.stereotype.Component

@Component
@Converter(autoApply = true)
class SplitAlgorithmConverter(
    private val objectMapper: ObjectMapper,
) : AttributeConverter<SplitAlgorithm?, String?> {

    override fun convertToDatabaseColumn(attribute: SplitAlgorithm?): String? = if (attribute != null) objectMapper.writeValueAsString(attribute) else null

    override fun convertToEntityAttribute(dbData: String?): SplitAlgorithm? = if (dbData != null) objectMapper.readValue(dbData, SplitAlgorithm::class.java) else null
}
