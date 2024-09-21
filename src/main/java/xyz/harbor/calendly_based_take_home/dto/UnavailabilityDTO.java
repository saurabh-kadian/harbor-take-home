package xyz.harbor.calendly_based_take_home.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.harbor.calendly_based_take_home.request.MarkUnavailabilityRequest;

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
        return startOfUnavailability.atZone(timezone).toEpochSecond();
    }

    public long getEndTimeInSeconds(){
        return endOfUnavailability.atZone(timezone).toEpochSecond();
    }

    public static UnavailabilityDTO from(MarkUnavailabilityRequest markUnavailabilityRequest){
        return UnavailabilityDTO.builder()
                .startOfUnavailability(
                    LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(Long.parseLong(markUnavailabilityRequest.getStartOfUnavailability())),
                        ZoneId.of(markUnavailabilityRequest.getTimezone())
                    )
                )
                .endOfUnavailability(
                    LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(Long.parseLong(markUnavailabilityRequest.getEndOfUnavailability())),
                            ZoneId.of(markUnavailabilityRequest.getTimezone())
                    )
                )
                .timezone(ZoneId.of(markUnavailabilityRequest.getTimezone()))
                .build();
    }

}
