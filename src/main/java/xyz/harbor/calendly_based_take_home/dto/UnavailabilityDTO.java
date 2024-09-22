package xyz.harbor.calendly_based_take_home.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.harbor.calendly_based_take_home.request.MarkUnavailabilityRequest;
import xyz.harbor.calendly_based_take_home.service.TimeCalculationService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UnavailabilityDTO {
    LocalDateTime startOfUnavailability;
    LocalDateTime endOfUnavailability;
    ZoneId timezone;

    public long getStartTimeInSeconds(){
        return TimeCalculationService.getTimeInSeconds(startOfUnavailability, timezone);
    }

    public long getEndTimeInSeconds(){
        return TimeCalculationService.getTimeInSeconds(endOfUnavailability, timezone);
    }

    public static UnavailabilityDTO from(MarkUnavailabilityRequest markUnavailabilityRequest){
        ZoneId timezone = ZoneId.of(markUnavailabilityRequest.getTimezone());
        return UnavailabilityDTO.builder()
                .startOfUnavailability(
                        TimeCalculationService.getTimeInLocalDateTime(
                                Long.parseLong(markUnavailabilityRequest.getStartOfUnavailability()),
                                timezone
                        )
                )
                .endOfUnavailability(
                        TimeCalculationService.getTimeInLocalDateTime(
                                Long.parseLong(markUnavailabilityRequest.getEndOfUnavailability()),
                                timezone
                        )
                )
                .timezone(timezone)
                .build();
    }

}
