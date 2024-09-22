package xyz.harbor.calendly_based_take_home.response;

import lombok.Builder;
import lombok.Value;
import xyz.harbor.calendly_based_take_home.model.SessionLength;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.ZoneId;

@Builder
@Value
public class UserResponse implements Serializable {
    String userId;
    String firstName;
    String lastName;
    String username;
    String email;
    SessionLength preferredSessionLength;
    LocalTime preferredWorkingHoursStartTime;
    LocalTime preferredWorkingHoursEndTime;
    ZoneId preferredTimezone;
}
