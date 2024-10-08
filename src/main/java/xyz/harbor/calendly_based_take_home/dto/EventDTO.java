package xyz.harbor.calendly_based_take_home.dto;

import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;
import xyz.harbor.calendly_based_take_home.model.Event;
import xyz.harbor.calendly_based_take_home.model.SessionLength;
import xyz.harbor.calendly_based_take_home.service.TimeCalculationService;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class EventDTO {

    private static final String NAME_KEY = "name";
    private static final String EMAIL_KEY = "email";
    private static final String START_TIME_KEY = "startTime";
    private static final String SESSION_LENGTH_KEY = "sessionLength";
    private static final String SELF_KEY = "self";

    String attendeeName;
    String attendeeEmail;
    LocalDateTime startTime;
    SessionLength sessionLength;
    ZoneId timezone;

    public static EventDTO empty(){
        return EventDTO.builder().build();
    }

    public static EventDTO fromEvent(Event event){
        return EventDTO.builder()
                .attendeeName(event.getAttendeeName())
                .attendeeEmail(event.getAttendeeEmail())
                .startTime(event.getStartTime())
                .sessionLength(event.getSessionLength())
                .timezone(event.getTimezone())
                .build();
    }

    public static boolean isEventBlockedByOthers(Event event){
        return !event.getAttendeeName().equals(SELF_KEY);
    }

    public static EventDTO forSelfUnavailability(Long startTime, SessionLength sessionLength, ZoneId timezone){
        return EventDTO.builder()
                .attendeeName(SELF_KEY)
                .attendeeEmail(SELF_KEY)
                .sessionLength(sessionLength)
                .startTime(TimeCalculationService.getTimeInLocalDateTime(startTime, timezone))
                .timezone(timezone)
                .build();
    }
}
