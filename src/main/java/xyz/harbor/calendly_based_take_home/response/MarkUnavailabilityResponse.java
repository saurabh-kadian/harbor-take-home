package xyz.harbor.calendly_based_take_home.response;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import xyz.harbor.calendly_based_take_home.dto.EventDTO;

import java.util.List;

@Data
@Builder
public class MarkUnavailabilityResponse {
    @NonNull
    String userId;
    @NonNull
    List<EventDTO> events;
}
