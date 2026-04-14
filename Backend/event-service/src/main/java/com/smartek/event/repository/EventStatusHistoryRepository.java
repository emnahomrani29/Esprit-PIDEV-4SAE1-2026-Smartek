package com.smartek.event.repository;

import com.smartek.event.model.EventStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventStatusHistoryRepository extends JpaRepository<EventStatusHistory, Long> {

    // Récupérer l'historique des changements de statut d'un événement
    List<EventStatusHistory> findByEventIdOrderByChangedAtDesc(Long eventId);

    // Récupérer le dernier changement de statut
    EventStatusHistory findFirstByEventIdOrderByChangedAtDesc(Long eventId);
}