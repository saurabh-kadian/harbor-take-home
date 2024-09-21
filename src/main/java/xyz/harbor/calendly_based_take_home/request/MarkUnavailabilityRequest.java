package xyz.harbor.calendly_based_take_home.request;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class MarkUnavailabilityRequest {
    @NonNull String startOfUnavailability;
    @NonNull String endOfUnavailability;
    @NonNull String userId;
    @NonNull String timezone;
}
