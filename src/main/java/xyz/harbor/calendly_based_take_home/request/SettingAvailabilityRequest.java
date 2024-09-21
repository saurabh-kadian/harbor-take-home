package xyz.harbor.calendly_based_take_home.request;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Builder
@Data
public class SettingAvailabilityRequest {
    @NonNull
    String username;
    @NonNull
    String password;
}
