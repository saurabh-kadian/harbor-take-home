package xyz.harbor.calendly_based_take_home.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import xyz.harbor.calendly_based_take_home.dto.EventDTO;
import xyz.harbor.calendly_based_take_home.dto.UnavailabilityDTO;
import xyz.harbor.calendly_based_take_home.model.Event;
import xyz.harbor.calendly_based_take_home.model.SessionLength;
import xyz.harbor.calendly_based_take_home.model.User;
import xyz.harbor.calendly_based_take_home.repository.EventRepository;
import xyz.harbor.calendly_based_take_home.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    public List<EventDTO> markUnavailability(UnavailabilityDTO unavailabilityDTO, String userId){
        Optional<User> userOptional = userRepository.findById(userId);
        if(userOptional.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found. Cannot enter unavailability of unknown user.");

        List<Event> eventsBetween = eventRepository.findEventsBetween(unavailabilityDTO.getStartTimeInSeconds(), unavailabilityDTO.getEndTimeInSeconds());
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
                .map(eventDTO -> {
                    Event event =  Event
                                    .builder()
                                    .eventId(UUID.randomUUID().toString())
                                    .user(userOptional.get())
                                    .attendeeName(eventDTO.getAttendeeName())
                                    .attendeeEmail(eventDTO.getAttendeeEmail())
                                    .startTime(eventDTO.getStartTime())
                                    .sessionLength(eventDTO.getSessionLength())
                                    .build();
                    eventRepository.save(event);
                    return eventDTO;
                })
                .toList();
    }
}
