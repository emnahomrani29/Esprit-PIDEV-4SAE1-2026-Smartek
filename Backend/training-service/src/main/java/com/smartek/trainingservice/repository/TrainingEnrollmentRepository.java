package com.smartek.trainingservice.repository;

import com.smartek.trainingservice.entity.TrainingEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingEnrollmentRepository extends JpaRepository<TrainingEnrollment, Long> {

    @Query("SELECT te FROM TrainingEnrollment te JOIN FETCH te.training WHERE te.userId = :userId")
    List<TrainingEnrollment> findByUserId(@Param("userId") Long userId);

    @Query("SELECT te FROM TrainingEnrollment te JOIN FETCH te.training WHERE te.training.trainingId = :trainingId")
    List<TrainingEnrollment> findByTrainingTrainingId(@Param("trainingId") Long trainingId);

    @Query("SELECT te FROM TrainingEnrollment te JOIN FETCH te.training WHERE te.userId = :userId AND te.training.trainingId = :trainingId")
    Optional<TrainingEnrollment> findByUserIdAndTrainingTrainingId(@Param("userId") Long userId, @Param("trainingId") Long trainingId);

    boolean existsByUserIdAndTrainingTrainingId(Long userId, Long trainingId);
    void deleteByTrainingTrainingId(Long trainingId);
    
    long countByUserId(Long userId);
    long countByUserIdAndStatus(Long userId, String status);
    
    @Query("SELECT te FROM TrainingEnrollment te JOIN FETCH te.training WHERE te.training.createdBy = :trainerId")
    List<TrainingEnrollment> findAllByTrainerId(@Param("trainerId") Long trainerId);
    
    long countByTrainingTrainingId(Long trainingId);
    long countByTrainingTrainingIdAndStatus(Long trainingId, String status);
}
