package xyz.harbor.calendly_based_take_home.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreateUserRequest {
    @JsonProperty("username")
    String username;
    @JsonProperty("password")
    String password;
    @JsonProperty("first_name")
    String firstName;
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonProperty("last_name")
    String lastName;
    @JsonProperty("email")
    String email;
    @JsonProperty("preferred_session_length")
    String preferredSessionLength;
    @JsonProperty("preferred_start_working_hours")
    String preferredStartingWorkingHours;
    @JsonProperty("preferred_end_working_hours")
    String preferredEndingWorkingHours;
    @JsonProperty("preferred_timezone")
    String timezone;
}
