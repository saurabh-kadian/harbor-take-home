package xyz.harbor.calendly_based_take_home.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.harbor.calendly_based_take_home.dto.UnavailabilityDTO;
import xyz.harbor.calendly_based_take_home.repository.EventRepository;
import xyz.harbor.calendly_based_take_home.repository.UserRepository;
import xyz.harbor.calendly_based_take_home.request.MarkUnavailabilityRequest;

@Service
public class EventService {

    EventRepository eventRepository;
    UserRepository userRepository;

    @Autowired
    public EventService(EventRepository eventRepository, UserRepository userRepository){
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }



}
