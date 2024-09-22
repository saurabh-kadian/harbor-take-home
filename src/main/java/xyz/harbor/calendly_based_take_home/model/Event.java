package xyz.harbor.calendly_based_take_home.model;

import jakarta.persistence.*;
import lombok.*;
import xyz.harbor.calendly_based_take_home.converter.JSONSessionMapConverter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

@Entity
@Table(name="event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
    @Id
    @Column(name = "event_id", nullable = false)
    String eventId;

    @ManyToOne
    @JoinColumn(name="event_owner_id", nullable = false)
    User user;

    @Column(name = "attendee_name", nullable = false)
    String attendeeName;

    @Column(name = "attendee_email", nullable = false)
    String attendeeEmail;

    @Column(name = "start_time", nullable = false)
    LocalDateTime startTime;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "session_length")
    SessionLength sessionLength;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", updatable = true, nullable = false)
    private LocalDateTime updatedAt;

    @PreUpdate
    @PrePersist
    protected void onCreateOrUpdate() {
        updatedAt = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        if(createdAt == null){
            createdAt = updatedAt;
        }
    }
}
