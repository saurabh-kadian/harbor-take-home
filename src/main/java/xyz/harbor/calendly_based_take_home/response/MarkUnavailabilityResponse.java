package xyz.harbor.calendly_based_take_home.response;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Value;
import xyz.harbor.calendly_based_take_home.dto.EventDTO;

import java.io.Serializable;
import java.util.List;

@Builder
@Value
public class MarkUnavailabilityResponse implements Serializable {
    @NonNull
    String userId;
    @NonNull
    List<EventDTO> events;
}
