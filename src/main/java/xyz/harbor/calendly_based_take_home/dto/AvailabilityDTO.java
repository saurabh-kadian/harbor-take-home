package xyz.harbor.calendly_based_take_home.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.harbor.calendly_based_take_home.model.SessionLength;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AvailabilityDTO {
    LocalDateTime startTime;
    SessionLength sessionLength;
}
