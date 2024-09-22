package xyz.harbor.calendly_based_take_home.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.harbor.calendly_based_take_home.dto.AvailabilityDTO;
import xyz.harbor.calendly_based_take_home.dto.EventDTO;
import xyz.harbor.calendly_based_take_home.dto.UnavailabilityDTO;
import xyz.harbor.calendly_based_take_home.model.SessionLength;
import xyz.harbor.calendly_based_take_home.request.MakeBookingRequest;
import xyz.harbor.calendly_based_take_home.request.MarkUnavailabilityRequest;
import xyz.harbor.calendly_based_take_home.response.MarkUnavailabilityResponse;
import xyz.harbor.calendly_based_take_home.service.AvailabilityService;
import xyz.harbor.calendly_based_take_home.service.TimeCalculationService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping(path = "${version}/availability")
public class AvailabilityController {
    @Autowired
    AvailabilityService availabilityService;

    @RequestMapping(value = {"/mark-unavailability", "/mark-unavailability/"}, method = RequestMethod.POST)
    public ResponseEntity<MarkUnavailabilityResponse> markUnavailability(@RequestBody MarkUnavailabilityRequest markUnavailabilityRequest){
        return ResponseEntity
                .ok()
                .body(
                        MarkUnavailabilityResponse
                                .builder()
                                .userId(markUnavailabilityRequest.getUserId())
                                .events(availabilityService
                                        .markUnavailability(
                                                UnavailabilityDTO.from(markUnavailabilityRequest),
                                                markUnavailabilityRequest.getUserId()
                                        )
                                )
                                .build()
                );
    }

    @RequestMapping(value = {"/make-booking", "/make-booking/"}, method = RequestMethod.POST)
    public ResponseEntity<EventDTO> makeBooking(@RequestBody MakeBookingRequest makeBookingRequest){
        return ResponseEntity
                .ok()
                .body(
                        availabilityService.makeBooking(
                                EventDTO
                                        .builder()
                                        .startTime(TimeCalculationService.parseLocalDateTime(
                                                makeBookingRequest.getStartTime(),
                                                ZoneId.of(makeBookingRequest.getTimezone())
                                        ))
                                        .timezone(ZoneId.of(makeBookingRequest.getTimezone()))
                                        .attendeeName(makeBookingRequest.getAttendeeName())
                                        .attendeeEmail(makeBookingRequest.getAttendeeEmail())
                                        .sessionLength(makeBookingRequest.getSessionLength())
                                        .build(),
                                makeBookingRequest.getOwnerUserId()
                        )
                );
    }

    @RequestMapping(value = {"/get-meetups/", "/get-meetups"}, method = RequestMethod.GET)
    public ResponseEntity<List<EventDTO>> getMeetups(@RequestParam(name = "days") int days,
                                                     @RequestParam(name = "userId") String userId){
        return ResponseEntity.ok().body(availabilityService.getMeetups(days, userId));
    }

    @RequestMapping(value = {"/get-next-meetup/", "/get-next-meetup"}, method = RequestMethod.GET)
    public ResponseEntity<EventDTO> getNextMeetup(@RequestParam(name = "userId") String userId){
        return ResponseEntity.ok().body(availabilityService.getNextMeetup(userId));
    }

    @RequestMapping(value = {"/get/", "/get"}, method = RequestMethod.GET)
    public ResponseEntity<List<AvailabilityDTO>> getAvailability(@RequestParam(name = "time_today") String timeToday,
                                                                 @RequestParam(name = "timezone") String timezone,
                                                                 @RequestParam(name = "userId") String userId){
        LocalDateTime timeTodayLocal = TimeCalculationService.parseLocalDateTime(timeToday,ZoneId.of(timezone));
        return ResponseEntity.ok().body(availabilityService.getAvailability(timeTodayLocal, userId));
    }

    @RequestMapping(value = {"/show-available-between/", "/show-available-between"}, method = RequestMethod.GET)
    public ResponseEntity<List<AvailabilityDTO>> getOverlapBetween(@RequestParam(name = "user1") String userIdFirst,
                                                                   @RequestParam(name = "user2") String userIdSecond,
                                                                   @RequestParam(name = "session_length") String length) {
        return ResponseEntity.ok().body(
                availabilityService.getOverlapBetween(userIdFirst,
                                                      userIdSecond,
                                                      SessionLength.fromJsonString(length)));
    }
}
