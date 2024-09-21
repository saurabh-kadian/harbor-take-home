package xyz.harbor.calendly_based_take_home.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import xyz.harbor.calendly_based_take_home.model.Event;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {

    @Query("SELECT e FROM Event e WHERE e.startTime >= :start AND e.startTime <= :end")
    List<Event> findEventsBetween(@Param("start") Long start, @Param("end") Long end);
}
