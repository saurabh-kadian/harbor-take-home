package xyz.harbor.calendly_based_take_home.request;

import lombok.Builder;
import lombok.Data;
import xyz.harbor.calendly_based_take_home.model.SessionLength;

@Data
@Builder
public class MakeBookingRequest {
    String ownerUserId;
    String attendeeName;
    String attendeeEmail;
    String startTime;
    SessionLength sessionLength;
    String timezone;
}
