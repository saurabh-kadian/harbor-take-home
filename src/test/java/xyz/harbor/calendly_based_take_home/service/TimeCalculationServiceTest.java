package xyz.harbor.calendly_based_take_home.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TimeCalculationServiceTest {

    String time = "2024-09-22T00:00:00";
    String timezoneIST = "Asia/Kolkata";

    @Test
    void testConversionFromStringToLocalDateTime() {
        LocalDateTime localDateTime = LocalDateTime
                .parse(time,
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .atZone(ZoneId.of(timezoneIST))
                .toLocalDateTime();
        System.out.println(localDateTime);
    }

}