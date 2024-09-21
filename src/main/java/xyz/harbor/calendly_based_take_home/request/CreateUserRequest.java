package xyz.harbor.calendly_based_take_home.request;

import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Builder
@Data
public class CreateUserRequest {
    @NonNull String username;
    @NonNull String password;
    @NonNull String firstName;
    @Nullable String lastName;
    @NonNull String email;
}
