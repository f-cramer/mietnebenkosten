package de.cramer.nebenkosten.jpa.converter

import com.fasterxml.jackson.databind.ObjectMapper
import de.cramer.nebenkosten.entities.SplitAlgorithm
import org.springframework.stereotype.Component
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Component
@Converter(autoApply = true)
class SplitAlgorithmConverter(
    private val objectMapper: ObjectMapper,
) : AttributeConverter<SplitAlgorithm?, String?> {

    override fun convertToDatabaseColumn(attribute: SplitAlgorithm?): String? = if (attribute != null) objectMapper.writeValueAsString(attribute) else null

    override fun convertToEntityAttribute(dbData: String?): SplitAlgorithm? = if (dbData != null) objectMapper.readValue(dbData, SplitAlgorithm::class.java) else null
}
