package xyz.harbor.calendly_based_take_home.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import xyz.harbor.calendly_based_take_home.model.Event;
import xyz.harbor.calendly_based_take_home.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {
    @Query("SELECT e FROM Event e WHERE e.user = :user AND e.startTime >= :start AND e.startTime <= :end")
    List<Event> findEventsBetween(@Param("user") User user,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);

    Optional<Event> findTopByUserAndStartTimeGreaterThanEqualOrderByStartTimeAsc(User user, LocalDateTime start);
}
