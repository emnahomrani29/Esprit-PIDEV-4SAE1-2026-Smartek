package com.smartek.learningmicroservice.repository;

import com.smartek.learningmicroservice.entity.LearningStylePreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LearningStylePreferenceRepository 
        extends JpaRepository<LearningStylePreference, Long> {

    Optional<LearningStylePreference> findByLearnerId(Long learnerId);

    boolean existsByLearnerId(Long learnerId);
}
