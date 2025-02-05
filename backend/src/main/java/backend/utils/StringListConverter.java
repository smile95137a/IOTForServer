package src.main.java.backend.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Converter(autoApply = true)
public class StringListConverter implements AttributeConverter<List<String>, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert List<String> to JSON", e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        try {
            if (dbData != null && !dbData.isEmpty()) {
                return OBJECT_MAPPER.readValue(dbData, OBJECT_MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, String.class));
            } else {
                return new ArrayList<>();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert JSON to List<String>", e);
        }
    }
}
