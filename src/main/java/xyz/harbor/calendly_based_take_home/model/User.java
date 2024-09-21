package xyz.harbor.calendly_based_take_home.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

// TODO(skadian): Take care of Timezones for scheduling

@Entity
@Table(name="users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @Column(name = "user_id", nullable = false)
    String userId;

    @Column(name="email", unique = true, length = 100, nullable = false)
    String email;

    @Column(name="firstname", nullable = false)
    String firstName;

    @Column(name="lastname")
    String lastName;

    @Column(name="username", unique = true, nullable = false)
    String username;

    @Column(name="password", nullable = false)
    String hashedPassword;

    @Column(name="salt", nullable = false)
    String salt;

    @ElementCollection(targetClass = SessionLength.class)
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_session_length")
    private SessionLength preferredSessionLength;

    @Column(name= "preferred_working_hours_start_time")
    LocalDateTime preferredWorkingHoursStartTime;

    @Column(name = "preferred_working_hours_end_time")
    LocalDateTime preferredWorkingHoursEndTime;

    @Column(name = "default_meeting_link")
    String defaultMeetingLink;

    @Column(name = "preferred_timezone")
    ZoneId preferredTimezone;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        if(createdAt == null){
            createdAt = updatedAt;
        }
    }

    @PreUpdate
    protected void onUpdate(){
        updatedAt = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
    }
}
