package xyz.harbor.calendly_based_take_home.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import xyz.harbor.calendly_based_take_home.dto.AvailabilityDTO;
import xyz.harbor.calendly_based_take_home.dto.EventDTO;
import xyz.harbor.calendly_based_take_home.dto.UnavailabilityDTO;
import xyz.harbor.calendly_based_take_home.model.Event;
import xyz.harbor.calendly_based_take_home.model.SessionLength;
import xyz.harbor.calendly_based_take_home.model.User;
import xyz.harbor.calendly_based_take_home.repository.EventRepository;
import xyz.harbor.calendly_based_take_home.repository.UserRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class AvailabilityService {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Autowired
    public AvailabilityService(UserRepository userRepository,
                               EventRepository eventRepository){
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    private User validateUser(String userId){
        Optional<User> userOptional = userRepository.findById(userId);
        if(userOptional.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found. Cannot enter unavailability of unknown user.");
        return userOptional.get();
    }

    public List<EventDTO> markUnavailability(UnavailabilityDTO unavailabilityDTO, String userId){
        User user = validateUser(userId);
        List<Event> eventsBetween = eventRepository.findEventsBetween(unavailabilityDTO.getStartTimeInSeconds(),
                                                                      unavailabilityDTO.getEndTimeInSeconds());
        List<Event> eventsBetweenByOthers = eventsBetween.stream().filter(EventDTO::isSessionBlockedByOthers).toList();

        if(!eventsBetweenByOthers.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You have a meeting setup between mentioned period of unavailability.");

        // TODO(skadian): Throwing here, otherwise we can simply merge these time quanta(s) by remove earlier events
        //                taking maximum of endTime and minimum of startTime and allocating events again.
        if(!eventsBetween.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You already made some sections of this time unavailable, ma;ybe try with smaller values");

        List<Pair<SessionLength, Long>> blockedSessions = TimeCalculationService.getBlockedSessions(
                unavailabilityDTO.getStartTimeInSeconds(),
                unavailabilityDTO.getEndTimeInSeconds()
        );

        return blockedSessions
                .stream()
                .map(sessionRawPair -> EventDTO
                        .forSelfUnavailability(
                                sessionRawPair.getSecond(),
                                sessionRawPair.getFirst()
                        )
                )
                .peek(eventDTO -> {
                    Event event =  Event
                                    .builder()
                                    .eventId(UUID.randomUUID().toString())
                                    .user(user)
                                    .attendeeName(eventDTO.getAttendeeName())
                                    .attendeeEmail(eventDTO.getAttendeeEmail())
                                    .startTime(eventDTO.getStartTime())
                                    .sessionLength(eventDTO.getSessionLength())
                                    .build();
                    eventRepository.save(event);
                })
                .toList();
    }

    public List<EventDTO> getMeetups(int days, String userId, String timezone){
        if(days >= 62)
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Support missing for dates more than 2 months from now");
        User user = validateUser(userId);
        ZoneId zoneIdTimezone = ZoneId.of(timezone);
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime endTime = currentTime.plusDays(days);
        List<Event> eventsBetween = eventRepository.findEventsBetween(
                    TimeCalculationService.getTimeInSeconds(currentTime, zoneIdTimezone),
                    TimeCalculationService.getTimeInSeconds(endTime, zoneIdTimezone)
                );
        return eventsBetween.stream().map(EventDTO::fromEvent).toList();
    }

    public EventDTO getNextMeetup(String userId){
        User user = validateUser(userId);
        Event nextEvent = eventRepository.findNextEvent(
                TimeCalculationService.getTimeInSeconds(
                        LocalDateTime.now(),
                        user.getPreferredTimezone()
                )
        );
        return EventDTO.fromEvent(nextEvent);
    }

    // TODO(skadian): Assuming we are only keeping track of unix epoch timestamps, we send startOfDay timestamp
    //                to send a particular day
    // TODO(skadian): Clean up
    public List<AvailabilityDTO> getAvailability(LocalDateTime startOfDay, String userId){
        User user = validateUser(userId);
        if(!TimeCalculationService.isStartOfDay(startOfDay))
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Timestamp sent is not start of day");
        LocalDateTime nextDayStart = startOfDay.plusHours(24);
        List<Event> eventsBetween = eventRepository.findEventsBetween(
                TimeCalculationService.getTimeInSeconds(startOfDay, user.getPreferredTimezone()),
                TimeCalculationService.getTimeInSeconds(nextDayStart, user.getPreferredTimezone())
        );

        Long workingHoursStartTime = TimeCalculationService.getTimeInSeconds(startOfDay, user.getPreferredTimezone());
        Long workingHoursEndTime = TimeCalculationService.getTimeInSeconds(nextDayStart, user.getPreferredTimezone());

        eventsBetween.sort(Comparator.comparing(Event::getStartTime));
        List<AvailabilityDTO> availableEvents = new ArrayList<>();

        // Check if working hours are provided
        if(user.getPreferredWorkingHoursStartTime() != null) {
            workingHoursStartTime = TimeCalculationService.getTimeInSeconds(user.getPreferredWorkingHoursStartTime(), user.getPreferredTimezone());
            workingHoursEndTime = TimeCalculationService.getTimeInSeconds(user.getPreferredWorkingHoursEndTime(), user.getPreferredTimezone());
        }
        for(int i = 0;i < eventsBetween.size();i++){
            if(i == 0 && !workingHoursStartTime.equals(
                    TimeCalculationService.getTimeInSeconds(
                            eventsBetween.get(0).getStartTime(),
                            user.getPreferredTimezone()))) {
                List<AvailabilityDTO> availableSlots = TimeCalculationService.getBlockedSessions(
                    workingHoursStartTime,
                    TimeCalculationService.getTimeInSeconds(eventsBetween.get(i).getStartTime(), user.getPreferredTimezone())
                ).stream().map(session ->
                        AvailabilityDTO
                                .builder()
                                .startTime(TimeCalculationService.getTimeInLocalDateTime(session.getSecond(),
                                        user.getPreferredTimezone()))
                                .sessionLength(session.getFirst())
                                .build()).toList();
                availableEvents.addAll(availableSlots);
                continue;
            }
            if(i == eventsBetween.size() - 1 && !workingHoursEndTime.equals(
                    TimeCalculationService.getTimeInSeconds(
                            eventsBetween.get(i).getStartTime()
                                    .plusSeconds(eventsBetween.get(i).getSessionLength().timeInSeconds),
                            user.getPreferredTimezone()))){
                List<AvailabilityDTO> availableSlots = TimeCalculationService.getBlockedSessions(
                        TimeCalculationService.getTimeInSeconds(
                                eventsBetween.get(i).getStartTime()
                                        .plusSeconds(eventsBetween.get(i).getSessionLength().timeInSeconds),
                                user.getPreferredTimezone()),
                        workingHoursEndTime
                ).stream().map(session ->
                        AvailabilityDTO
                                .builder()
                                .startTime(TimeCalculationService.getTimeInLocalDateTime(session.getSecond(),
                                        user.getPreferredTimezone()))
                                .sessionLength(session.getFirst())
                                .build()).toList();
                availableEvents.addAll(availableSlots);
                continue;
            }
            List<AvailabilityDTO> availableSlots = TimeCalculationService.getBlockedSessions(
                    TimeCalculationService.getTimeInSeconds(eventsBetween.get(i).getStartTime()
                                    .plusSeconds(eventsBetween.get(i).getSessionLength().timeInSeconds),
                                        user.getPreferredTimezone()),
                    TimeCalculationService.getTimeInSeconds(eventsBetween.get(i + 1).getStartTime(), user.getPreferredTimezone())
            ).stream().map(session ->
                    AvailabilityDTO
                            .builder()
                            .startTime(TimeCalculationService.getTimeInLocalDateTime(session.getSecond(),
                                    user.getPreferredTimezone()))
                            .sessionLength(session.getFirst())
                            .build()).toList();
            availableEvents.addAll(availableSlots);
        }
        availableEvents = availableEvents
                .stream()
                .filter(event -> event.getSessionLength().timeInSeconds !=
                                 SessionLength.BLOCKING_MINUTE_1.timeInSeconds)
                .toList();
        return availableEvents;
    }
}
