package xyz.harbor.calendly_based_take_home.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JSONSessionMetadataConverter implements AttributeConverter<Map<String, String>, String> {
    @Override
    public String convertToDatabaseColumn(Map<String, String> sessionDetails) {
        ObjectMapper objectMapper = new ObjectMapper();
        String sessionDetailsJSON = null;
        try {
            sessionDetailsJSON = objectMapper.writeValueAsString(sessionDetails);
        } catch (final JsonProcessingException e) {
            log.error("JSON writing error", e);
        }

        return sessionDetailsJSON;
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String sessionDetailsJSON) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> sessionDetails = null;
        try {
            sessionDetails = objectMapper.readValue(sessionDetailsJSON, new TypeReference<HashMap<String, String>>() {});
        } catch (final IOException e) {
            log.error("JSON reading error", e);
        }

        return sessionDetails;
    }
}
