package com.smartek.planning.repository;

import com.smartek.planning.model.PlanningRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlanningRegistrationRepository extends JpaRepository<PlanningRegistration, Long> {
    
    /**
     * Vérifier si un learner est déjà inscrit à une session
     */
    @Query("SELECT pr FROM PlanningRegistration pr WHERE pr.planningId = :planningId AND pr.learnerId = :learnerId AND pr.status != 'CANCELLED'")
    Optional<PlanningRegistration> findActiveRegistration(Long planningId, Long learnerId);
    
    /**
     * Compter le nombre d'inscrits actifs pour une session
     */
    @Query("SELECT COUNT(pr) FROM PlanningRegistration pr WHERE pr.planningId = :planningId AND pr.status = 'REGISTERED'")
    Long countActiveRegistrations(Long planningId);
    
    /**
     * Récupérer toutes les inscriptions d'un learner
     */
    @Query("SELECT pr FROM PlanningRegistration pr WHERE pr.learnerId = :learnerId AND pr.status != 'CANCELLED' ORDER BY pr.registrationDate DESC")
    List<PlanningRegistration> findByLearnerId(Long learnerId);
    
    /**
     * Récupérer les inscriptions d'un learner pour une période donnée
     */
    @Query("SELECT pr FROM PlanningRegistration pr " +
           "JOIN Planning p ON pr.planningId = p.planningId " +
           "WHERE pr.learnerId = :learnerId AND pr.status != 'CANCELLED' " +
           "AND p.date BETWEEN :startDate AND :endDate " +
           "ORDER BY p.date ASC, p.startTime ASC")
    List<PlanningRegistration> findByLearnerIdAndDateRange(Long learnerId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Récupérer les inscriptions pour une session spécifique
     */
    @Query("SELECT pr FROM PlanningRegistration pr WHERE pr.planningId = :planningId AND pr.status != 'CANCELLED' ORDER BY pr.registrationDate ASC")
    List<PlanningRegistration> findByPlanningId(Long planningId);
    
    /**
     * Récupérer la prochaine position en liste d'attente
     */
    @Query("SELECT COALESCE(MAX(pr.waitingListPosition), 0) + 1 FROM PlanningRegistration pr WHERE pr.planningId = :planningId AND pr.status = 'WAITING_LIST'")
    Integer getNextWaitingListPosition(Long planningId);
}