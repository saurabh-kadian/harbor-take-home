package xyz.harbor.calendly_based_take_home.service;

import org.springframework.stereotype.Service;
import xyz.harbor.calendly_based_take_home.model.SessionLength;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class TimeCalculationService {

    public static LocalDateTime setToStartOfDay(LocalDateTime dateTime){
        return dateTime.withHour(0).withMinute(0).withSecond(0);
    }

    public static LocalDateTime convertLocalTimeToLocalDateTime(LocalTime localTime){
        return LocalDateTime.now()
                .withHour(localTime.getHour())
                .withMinute(localTime.getMinute())
                .withSecond(localTime.getSecond());
    }

    public static Long getTimeInSeconds(LocalDateTime day, LocalTime localTime, ZoneId zoneId){
        LocalDateTime dateTimeWithTime = day.withHour(localTime.getHour())
                .withMinute(localTime.getMinute())
                .withSecond(localTime.getSecond());
        return dateTimeWithTime.atZone(zoneId).toEpochSecond();
    }

    public static Long getTimeInSeconds(LocalDateTime localDateTime, ZoneId zoneId){
        return localDateTime.atZone(zoneId).toEpochSecond();
    }

    public static LocalDateTime getTimeInLocalDateTime(Long timeInSeconds, ZoneId zoneId){
        return LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(timeInSeconds),
                    zoneId
            );
    }

    public static LocalDateTime getEndTimeLocalDateTime(LocalDateTime localDateTime, SessionLength sessionLength){
        return localDateTime.plusSeconds(sessionLength.timeInSeconds);
    }

    public static Long getEndTimeInSeconds(SessionLength sessionLength, Long startTime){
        return startTime + sessionLength.timeInSeconds;
    }

    public static List<Long> getAvailableSessions(Long startTime, Long endTime, SessionLength sessionLength){
        List<Long> blockedSessions = new ArrayList<>();
        while(startTime <= endTime) {
            if(!pullSession(startTime, endTime, sessionLength))
                break;
            blockedSessions.add(startTime);
            startTime += sessionLength.timeInSeconds;
        }
        return blockedSessions;
    }

    // TODO(skadian): For extensibility, take all the values of the enum, sort by timeInSeconds in descending and start
    //                allocating
    private static Boolean pullSession(Long startTime, Long endTime, SessionLength sessionLength){
        if(endTime - startTime >= sessionLength.timeInSeconds)
            return Boolean.TRUE;
        return Boolean.FALSE;
    }

    public static boolean isStartOfDay(LocalDateTime claimedStartOfDay){
        return claimedStartOfDay.toLocalTime().equals(LocalTime.MIDNIGHT);
    }

}
