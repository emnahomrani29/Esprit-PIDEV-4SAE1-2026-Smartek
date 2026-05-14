package com.smartek.event.repository;

import com.smartek.event.model.EventRegistration;
import com.smartek.event.model.RegistrationStatus;
import com.smartek.event.model.PaymentStatus;
import com.smartek.event.model.EventMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    // Vérifier si un utilisateur est déjà inscrit à un événement
    Optional<EventRegistration> findByEventIdAndUserId(Long eventId, Long userId);

    // Récupérer toutes les inscriptions d'un événement
    List<EventRegistration> findByEventIdOrderByRegisteredAtAsc(Long eventId);

    // Récupérer les inscriptions par statut
    List<EventRegistration> findByEventIdAndStatus(Long eventId, RegistrationStatus status);

    // Récupérer la liste d'attente triée par ordre d'inscription
    @Query("SELECT r FROM EventRegistration r WHERE r.eventId = :eventId AND r.status = 'WAITING' ORDER BY r.registeredAt ASC")
    List<EventRegistration> findWaitingListByEventId(@Param("eventId") Long eventId);

    // Compter les inscriptions confirmées par mode de participation
    @Query("SELECT COUNT(r) FROM EventRegistration r WHERE r.eventId = :eventId AND r.status = 'CONFIRMED' AND r.participationMode = :mode")
    Long countConfirmedByEventIdAndMode(@Param("eventId") Long eventId, @Param("mode") EventMode mode);

    // Compter les inscriptions payées
    @Query("SELECT COUNT(r) FROM EventRegistration r WHERE r.eventId = :eventId AND r.paymentStatus = 'PAID'")
    Long countPaidRegistrationsByEventId(@Param("eventId") Long eventId);

    // Récupérer les inscriptions d'un utilisateur
    List<EventRegistration> findByUserIdOrderByRegisteredAtDesc(Long userId);

    // Compter les annulations
    @Query("SELECT COUNT(r) FROM EventRegistration r WHERE r.eventId = :eventId AND r.status = 'CANCELLED'")
    Long countCancelledByEventId(@Param("eventId") Long eventId);

    // Récupérer le premier en liste d'attente
    @Query("SELECT r FROM EventRegistration r WHERE r.eventId = :eventId AND r.status = 'WAITING' ORDER BY r.registeredAt ASC LIMIT 1")
    Optional<EventRegistration> findFirstWaitingByEventId(@Param("eventId") Long eventId);
}