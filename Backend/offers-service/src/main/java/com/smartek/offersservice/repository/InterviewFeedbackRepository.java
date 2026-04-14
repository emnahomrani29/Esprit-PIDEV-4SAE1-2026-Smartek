package com.smartek.offersservice.repository;

import com.smartek.offersservice.entity.InterviewFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewFeedbackRepository extends JpaRepository<InterviewFeedback, Long> {

    Optional<InterviewFeedback> findByInterviewId(Long interviewId);

    List<InterviewFeedback> findByApplicationId(Long applicationId);

    List<InterviewFeedback> findBySubmittedBy(Long submittedBy);

    long countByDecision(InterviewFeedback.FeedbackDecision decision);
}
