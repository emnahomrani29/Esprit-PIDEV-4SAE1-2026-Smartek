package com.smartek.courseservice.repository;

import com.smartek.courseservice.entity.LiveSession;
import com.smartek.courseservice.entity.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LiveSessionRepository extends JpaRepository<LiveSession, Long> {
    
    /**
     * Récupère toutes les sessions d'un cours - native SQL avec JOIN
     */
    @Query(value = "SELECT ls.* FROM live_sessions ls INNER JOIN courses c ON ls.course_id = c.course_id WHERE ls.course_id = :courseId ORDER BY ls.start_time ASC", nativeQuery = true)
    List<LiveSession> findByCourseId(@Param("courseId") Long courseId);
    
    /**
     * Récupère les sessions à venir d'un cours - native SQL
     */
    @Query(value = "SELECT ls.* FROM live_sessions ls WHERE ls.course_id = :courseId AND ls.status = 'SCHEDULED' AND ls.start_time > :now ORDER BY ls.start_time ASC", nativeQuery = true)
    List<LiveSession> findUpcomingSessionsByCourseId(@Param("courseId") Long courseId,
                                                      @Param("now") LocalDateTime now);
    
    /**
     * Récupère les sessions en cours d'un cours - native SQL
     */
    @Query(value = "SELECT ls.* FROM live_sessions ls WHERE ls.course_id = :courseId AND ls.status = 'ONGOING' ORDER BY ls.start_time ASC", nativeQuery = true)
    List<LiveSession> findOngoingSessionsByCourseId(@Param("courseId") Long courseId);
    
    /**
     * Récupère toutes les sessions d'un trainer - native SQL
     */
    @Query(value = "SELECT ls.* FROM live_sessions ls WHERE ls.trainer_id = :trainerId ORDER BY ls.start_time DESC", nativeQuery = true)
    List<LiveSession> findByTrainerIdOrderByStartTimeDesc(@Param("trainerId") Long trainerId);
    
    /**
     * Récupère les sessions par statut
     */
    List<LiveSession> findByStatus(SessionStatus status);
    
    /**
     * Récupère les sessions d'un cours par statut - native SQL
     */
    @Query(value = "SELECT * FROM live_sessions WHERE course_id = :courseId AND status = :status ORDER BY start_time ASC", nativeQuery = true)
    List<LiveSession> findByCourseIdAndStatus(@Param("courseId") Long courseId,
                                               @Param("status") String status);
}
