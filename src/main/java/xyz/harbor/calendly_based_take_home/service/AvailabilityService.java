package xyz.harbor.calendly_based_take_home.service;

import org.springframework.beans.factory.annotation.Autowired;
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

    /**
     * Note: This function as of now throws when self unavailability collisions happen. To bypass, we can simply merge
     * these intersecting time quanta(s) by removing earlier persisted events and taking the maximum of endTime and
     * minimum of startTime, making these, our new events.
     * @param unavailabilityDTO
     * @param userId
     * @return List<EventDTO>
     */
    public List<EventDTO> markUnavailability(UnavailabilityDTO unavailabilityDTO, String userId){
        User user = validateUser(userId);
        List<Event> eventsBetween = eventRepository.findEventsBetween(user,
                unavailabilityDTO.getStartOfUnavailability(),
                unavailabilityDTO.getEndOfUnavailability());
        List<Event> eventsBetweenByOthers = eventsBetween.stream().filter(EventDTO::isEventBlockedByOthers).toList();

        if(!eventsBetweenByOthers.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    "You have a meeting setup between mentioned period of unavailability.");

        if(!eventsBetween.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    "You already made some sections of this time unavailable, ma;ybe try with smaller values");

        List<Long> availableSessions = TimeCalculationService.getAvailableSessions(
                unavailabilityDTO.getStartTimeInSeconds(),
                unavailabilityDTO.getEndTimeInSeconds(),
                user.getPreferredSessionLength()
        );

        return availableSessions
                .stream()
                .map(sessionStartTime -> EventDTO
                        .forSelfUnavailability(
                                sessionStartTime,
                                user.getPreferredSessionLength(),
                                user.getPreferredTimezone()
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

    /**
     * Note: Here meetups will be shown in users preferred timezone.
     * @param days
     * @param userId
     * @return List<EventDTO>
     */
    public List<EventDTO> getMeetups(int days, String userId){
        if(days >= 62)
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    "Support unavailable for dates more than 2 months into the future.");
        User user = validateUser(userId);
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime endTime = currentTime.plusDays(days);
        List<Event> eventsBetween = eventRepository.findEventsBetween(user, currentTime, endTime);
        return eventsBetween.stream().map(EventDTO::fromEvent).toList();
    }

    /**
     * Note: Here next meetup will be shown in users preferred timezone.
     * @param userId
     * @return EventDTO
     */
    public EventDTO getNextMeetup(String userId){
        User user = validateUser(userId);
        Optional<Event> nextEvent = eventRepository.findTopByUserAndStartTimeGreaterThanEqualOrderByStartTimeAsc(user,
                LocalDateTime.now());
        if(nextEvent.isEmpty())
            return EventDTO.empty();
        return EventDTO.fromEvent(nextEvent.get());
    }

    /**
     * Note: Here availability will be shown in users preferred timezone.
     * @param localDateTime
     * @param userId
     * @return List<AvailabilityDTO>
     */
    public List<AvailabilityDTO> getAvailability(LocalDateTime localDateTime, String userId){
        return this.getAvailability(localDateTime, userId, Optional.empty());
    }

    /**
     * Note: Here availability will be shown in users preferred timezone.
     * @param localDateTime
     * @param userId
     * @param sessionLengthOptional
     * @return List<AvailabilityDTO>
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public List<AvailabilityDTO> getAvailability(LocalDateTime localDateTime,
                                                 String userId,
                                                 Optional<SessionLength> sessionLengthOptional){
        User user = validateUser(userId);
        LocalDateTime startOfDay = TimeCalculationService.setToStartOfDay(localDateTime);
        LocalDateTime startOfNextDay = startOfDay.plusHours(24);

        List<Event> eventsBetween = eventRepository.findEventsBetween(user, startOfDay, startOfNextDay);
        SessionLength sessionLength = sessionLengthOptional.orElseGet(user::getPreferredSessionLength);

        Long workingHoursStartTime = TimeCalculationService.getTimeInSeconds(startOfDay, user.getPreferredTimezone());
        Long workingHoursEndTime = TimeCalculationService.getTimeInSeconds(startOfNextDay, user.getPreferredTimezone());

        eventsBetween.sort(Comparator.comparing(Event::getStartTime));

        // Check if working hours are provided
        if(user.getPreferredWorkingHoursStartTime() != null) {
            workingHoursStartTime = TimeCalculationService.getTimeInSeconds(startOfDay,
                    user.getPreferredWorkingHoursStartTime(),
                    user.getPreferredTimezone());
            workingHoursEndTime = TimeCalculationService.getTimeInSeconds(startOfDay,
                    user.getPreferredWorkingHoursEndTime(),
                    user.getPreferredTimezone());
        }

        // If no events are scheduled, mark everything as available
        if(eventsBetween.isEmpty()){
            return TimeCalculationService.getAvailableSessions(workingHoursStartTime, workingHoursEndTime,
                        sessionLength)
                    .stream()
                    .map(sessionStartTime ->AvailabilityDTO
                                                .builder()
                                                .startTime(
                                                        TimeCalculationService.getTimeInLocalDateTime(
                                                                sessionStartTime,
                                                                user.getPreferredTimezone())
                                                )
                                                .sessionLength(sessionLength)
                                                .build())
                    .toList();
        }

        return this.getAvailableSlotsForUserInBetweenEvents(user, eventsBetween, workingHoursStartTime,
                                                            workingHoursEndTime, sessionLength);
    }

    /**
     * Note: Timezone adjustment is pending.
     * @param userIdFirst
     * @param userIdSecond
     * @param sessionLength
     * @return List<AvailabilityDTO>
     */
    public List<AvailabilityDTO> getOverlapBetween(String userIdFirst, String userIdSecond, SessionLength sessionLength){
        User firstUser = validateUser(userIdFirst);
        User secondUser = validateUser(userIdSecond);

        LocalDateTime midnight = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);

        // Convert to modifiable list
        List<AvailabilityDTO> firstUserAvailability = new ArrayList<>(this.getAvailability(midnight, userIdFirst,
                Optional.of(sessionLength)));
        List<AvailabilityDTO> secondUserAvailability = new ArrayList<>(this.getAvailability(midnight, userIdSecond,
                Optional.of(sessionLength)));

        firstUserAvailability.sort(Comparator.comparing(AvailabilityDTO::getStartTime));
        secondUserAvailability.sort(Comparator.comparing(AvailabilityDTO::getStartTime));

        return this.getOverlappingSlotsBetweenTwoUsers(firstUser, secondUser, firstUserAvailability,
                secondUserAvailability, sessionLength);

    }

    private List<AvailabilityDTO> getOverlappingSlotsBetweenTwoUsers(User firstUser,
                                                                     User secondUser,
                                                                     List<AvailabilityDTO> firstUserAvailability,
                                                                     List<AvailabilityDTO> secondUserAvailability,
                                                                     SessionLength sessionLength){
        List<AvailabilityDTO> overlap = new ArrayList<>();

        int firstUserAvailabilityIndex = 0;
        int secondUserAvailabilityIndex = 0;
        int firstUserAvailabilitySize = firstUserAvailability.size();
        int secondUserAvailabilitySize = secondUserAvailability.size();

        while(firstUserAvailabilityIndex < firstUserAvailabilitySize &&
                secondUserAvailabilityIndex < secondUserAvailabilitySize){

            // Initialize for ease of reading
            Long firstUserEventStart = TimeCalculationService.getTimeInSeconds(
                    firstUserAvailability.get(firstUserAvailabilityIndex).getStartTime(),
                    firstUser.getPreferredTimezone()
            );
            Long secondUserEventStart = TimeCalculationService.getTimeInSeconds(
                    secondUserAvailability.get(secondUserAvailabilityIndex).getStartTime(),
                    secondUser.getPreferredTimezone()
            );
            Long firstUserEventEnd = TimeCalculationService.getTimeInSeconds(
                    TimeCalculationService.getEndTimeLocalDateTime(
                            firstUserAvailability.get(firstUserAvailabilityIndex).getStartTime(),
                            firstUserAvailability.get(firstUserAvailabilityIndex).getSessionLength()),
                    firstUser.getPreferredTimezone()
            );
            Long secondUserEventEnd = TimeCalculationService.getTimeInSeconds(
                    TimeCalculationService.getEndTimeLocalDateTime(
                            secondUserAvailability.get(secondUserAvailabilityIndex).getStartTime(),
                            secondUserAvailability.get(secondUserAvailabilityIndex).getSessionLength()),
                    secondUser.getPreferredTimezone()
            );

            // Core logic
            Long intersectStart = Math.max(firstUserEventStart,secondUserEventStart);
            Long intersectEnd = Math.min(firstUserEventEnd, secondUserEventEnd);

            if(intersectEnd - intersectStart >= sessionLength.timeInSeconds){
                overlap.add(AvailabilityDTO.builder()
                        .startTime(TimeCalculationService.getTimeInLocalDateTime(
                                intersectStart,
                                firstUser.getPreferredTimezone()))
                        .sessionLength(sessionLength)
                        .build());
            }

            if(firstUserEventEnd < secondUserEventEnd)
                firstUserAvailabilityIndex += 1;
            else
                secondUserAvailabilityIndex += 1;
        }
        return overlap;
    }


    private List<AvailabilityDTO> getAvailableSlotsForUserInBetweenEvents(User user,
                                                                          List<Event> eventsBetween,
                                                                          Long workingHoursStartTime,
                                                                          Long workingHoursEndTime,
                                                                          SessionLength sessionLength){
        List<AvailabilityDTO> availableEvents = new ArrayList<>();
        for(int i = 0;i < eventsBetween.size();i++){
            LocalDateTime eventStartTime = eventsBetween.get(i).getStartTime();
            LocalDateTime eventEndTime = eventsBetween.get(i).getStartTime()
                    .plusSeconds(eventsBetween.get(i).getSessionLength().timeInSeconds);
            if(i == 0 && !workingHoursStartTime.equals(
                    TimeCalculationService.getTimeInSeconds(eventStartTime,user.getPreferredTimezone()))) {
                List<AvailabilityDTO> availableSlots = this.divideSessionByLength(
                        workingHoursStartTime,
                        eventStartTime,
                        sessionLength,
                        user.getPreferredTimezone()
                );
                availableEvents.addAll(availableSlots);
                continue;
            }
            if(i == eventsBetween.size() - 1){
                // Separate out to make guard statement
                if(!workingHoursEndTime.equals(
                        TimeCalculationService.getTimeInSeconds(eventEndTime,user.getPreferredTimezone()))) {
                    List<AvailabilityDTO> availableSlots = this.divideSessionByLength(
                            eventEndTime,
                            workingHoursEndTime,
                            sessionLength,
                            user.getPreferredTimezone()
                    );
                    availableEvents.addAll(availableSlots);
                }
                continue;
            }
            List<AvailabilityDTO> availableSlots = this.divideSessionByLength(
                    eventEndTime,
                    eventsBetween.get(i + 1).getStartTime(),
                    sessionLength,
                    user.getPreferredTimezone()
            );
            availableEvents.addAll(availableSlots);
        }
        return availableEvents;
    }

    private List<AvailabilityDTO> divideSessionByLength(Long startTime,
                                                        LocalDateTime endTime,
                                                        SessionLength sessionLength,
                                                        ZoneId timezone){
        return this.divideSessionByLength(
                startTime,
                TimeCalculationService.getTimeInSeconds(endTime,timezone),
                sessionLength,
                timezone);
    }

    private List<AvailabilityDTO> divideSessionByLength(LocalDateTime startTime,
                                                        Long endTime,
                                                        SessionLength sessionLength,
                                                        ZoneId timezone){
        return this.divideSessionByLength(
                TimeCalculationService.getTimeInSeconds(startTime,timezone),
                endTime,
                sessionLength,
                timezone);
    }

    private List<AvailabilityDTO> divideSessionByLength(LocalDateTime startTime,
                                                        LocalDateTime endTime,
                                                        SessionLength sessionLength,
                                                        ZoneId timezone){
        return this.divideSessionByLength(
                TimeCalculationService.getTimeInSeconds(startTime,timezone),
                TimeCalculationService.getTimeInSeconds(endTime,timezone),
                sessionLength,
                timezone);
    }

    private List<AvailabilityDTO> divideSessionByLength(Long startTime,
                                                        Long endTime,
                                                        SessionLength sessionLength,
                                                        ZoneId timezone){
        return TimeCalculationService.getAvailableSessions(
                    startTime,
                    endTime,
                    sessionLength)
                .stream()
                .map(sessionStartTime -> AvailabilityDTO
                                            .builder()
                                            .startTime(TimeCalculationService.getTimeInLocalDateTime(sessionStartTime,
                                                    timezone))
                                            .sessionLength(sessionLength)
                                            .build())
                .toList();
    }

    private User validateUser(String userId){
        Optional<User> userOptional = userRepository.findById(userId);
        if(userOptional.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "User not found. Cannot enter unavailability of unknown user.");
        return userOptional.get();
    }
}
