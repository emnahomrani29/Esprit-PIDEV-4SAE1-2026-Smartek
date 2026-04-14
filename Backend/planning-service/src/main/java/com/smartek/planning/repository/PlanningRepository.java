package com.smartek.planning.repository;

import com.smartek.planning.model.Planning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface PlanningRepository extends JpaRepository<Planning, Long> {
    
    @Query("SELECT p FROM Planning p WHERE p.date >= :date ORDER BY p.date ASC, p.startTime ASC")
    List<Planning> findUpcomingPlannings(LocalDate date);
    
    @Query("SELECT p FROM Planning p WHERE p.date = :date ORDER BY p.startTime ASC")
    List<Planning> findPlanningsByDate(LocalDate date);
    
    @Query("SELECT p FROM Planning p WHERE p.date BETWEEN :startDate AND :endDate ORDER BY p.date ASC, p.startTime ASC")
    List<Planning> findPlanningsByDateRange(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT p FROM Planning p WHERE p.date = :date AND p.eventType = :eventType ORDER BY p.startTime ASC")
    List<Planning> findPlanningsByDateAndType(LocalDate date, String eventType);
    
    @Query("SELECT p FROM Planning p WHERE p.date = :date AND " +
           "((p.startTime <= :startTime AND p.endTime > :startTime) OR " +
           "(p.startTime < :endTime AND p.endTime >= :endTime) OR " +
           "(p.startTime >= :startTime AND p.endTime <= :endTime))")
    List<Planning> findConflictingPlannings(LocalDate date, LocalTime startTime, LocalTime endTime);
    
    // Nouvelles méthodes pour le planning hebdomadaire
    @Query("SELECT p FROM Planning p WHERE p.trainerId = :trainerId AND p.date BETWEEN :startDate AND :endDate ORDER BY p.date ASC, p.startTime ASC")
    List<Planning> findByTrainerIdAndDateBetween(Long trainerId, LocalDate startDate, LocalDate endDate);
    
    @Modifying
    @Query("DELETE FROM Planning p WHERE p.trainerId = :trainerId AND p.date BETWEEN :startDate AND :endDate")
    void deleteByTrainerIdAndDateBetween(Long trainerId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT p FROM Planning p WHERE p.status = 'PUBLISHED' ORDER BY p.date ASC, p.startTime ASC")
    List<Planning> findPublishedPlannings();
    
    // Méthode pour les learners - récupérer les plannings publiés pour une période donnée
    @Query("SELECT p FROM Planning p WHERE p.status = :status AND p.date BETWEEN :startDate AND :endDate ORDER BY p.date ASC, p.startTime ASC")
    List<Planning> findByStatusAndDateBetween(String status, LocalDate startDate, LocalDate endDate);
}
