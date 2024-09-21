package xyz.harbor.calendly_based_take_home.service;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import xyz.harbor.calendly_based_take_home.model.SessionLength;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TimeCalculationService {

    public static Long getTimeInSeconds(LocalDateTime localDateTime, ZoneId zoneId){
        return localDateTime.atZone(zoneId).toEpochSecond();
    }

    public static LocalDateTime getTimeInLocalDateTime(Long timeInSeconds, ZoneId zoneId){
        return LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(timeInSeconds),
                    zoneId
            );
    }

    public static Long getEndTimeInSeconds(SessionLength sessionLength, Long startTime){
        return startTime + sessionLength.timeInSeconds;
    }

    public static List<Pair<SessionLength, Long>> getBlockedSessions(Long startTime, Long endTime){
        List<Pair<SessionLength, Long>> blockedSessions = new ArrayList<>();
        while(startTime >= endTime) {
            Optional<SessionLength> biggestSessionOptional = pullBiggestSession(startTime, endTime);
            if(biggestSessionOptional.isEmpty())
                break;
            SessionLength biggestSession = biggestSessionOptional.get();
            blockedSessions.add(Pair.of(biggestSession, startTime));
            startTime += biggestSession.timeInSeconds;
        }
        return blockedSessions;
    }

    // TODO(skadian): For extensibility, take all the values of the enum, sort by timeInSeconds in descending and start
    //                allocating
    private static Optional<SessionLength> pullBiggestSession(Long startTime, Long endTime){
        Long difference = endTime - startTime;
        if(difference <= 0)
            return Optional.empty();
        if(difference >= SessionLength.MINUTES_60.timeInSeconds)
            return Optional.of(SessionLength.MINUTES_60);
        if(difference >= SessionLength.MINUTES_45.timeInSeconds)
            return Optional.of(SessionLength.MINUTES_45);
        if(difference >= SessionLength.MINUTES_30.timeInSeconds)
            return Optional.of(SessionLength.MINUTES_30);
        if(difference >= SessionLength.MINUTES_15.timeInSeconds)
            return Optional.of(SessionLength.MINUTES_15);
        return Optional.of(SessionLength.BLOCKING_MINUTE_1);
    }

    public static boolean isStartOfDay(LocalDateTime claimedStartOfDay){
        return claimedStartOfDay.toLocalTime().equals(LocalTime.MIDNIGHT);
    }

}
