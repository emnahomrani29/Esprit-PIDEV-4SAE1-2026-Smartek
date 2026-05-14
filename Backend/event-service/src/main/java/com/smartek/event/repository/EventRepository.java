package com.smartek.event.repository;

import com.smartek.event.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    List<Event> findByStartDateAfter(LocalDateTime date);
    
    List<Event> findByStartDateBetween(LocalDateTime start, LocalDateTime end);
    
    List<Event> findByLocationContainingIgnoreCase(String location);
}
