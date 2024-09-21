package xyz.harbor.calendly_based_take_home.response;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;

@Builder
@Value
public class UserResponse implements Serializable {
    String userId;
    String firstName;
    String lastName;
    String username;
    Integer reputation;
    String email;
}
