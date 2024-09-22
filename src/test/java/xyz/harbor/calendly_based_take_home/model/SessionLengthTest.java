package xyz.harbor.calendly_based_take_home.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@ExtendWith(MockitoExtension.class)
public class SessionLengthTest {
    @Test
    void testConversionFromStringToLocalDateTime() throws JsonProcessingException {
        System.out.println(new ObjectMapper().readValue("1", SessionLength.class));
    }
}
