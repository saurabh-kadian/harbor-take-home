package xyz.harbor.calendly_based_take_home.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import xyz.harbor.calendly_based_take_home.dto.EventDTO;
import xyz.harbor.calendly_based_take_home.dto.UnavailabilityDTO;
import xyz.harbor.calendly_based_take_home.model.Event;
import xyz.harbor.calendly_based_take_home.request.MarkUnavailabilityRequest;
import xyz.harbor.calendly_based_take_home.response.MarkUnavailabilityResponse;
import xyz.harbor.calendly_based_take_home.response.UserResponse;
import xyz.harbor.calendly_based_take_home.service.AvailabilityService;
import xyz.harbor.calendly_based_take_home.service.EventService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "${version}/availability")
public class AvailabilityController {
    @Autowired
    AvailabilityService availabilityService;

    @RequestMapping(value = {"/mark-unavailability"}, method = RequestMethod.POST)
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
}
