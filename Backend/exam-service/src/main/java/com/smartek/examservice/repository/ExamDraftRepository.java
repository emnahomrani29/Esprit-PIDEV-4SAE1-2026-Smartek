package com.smartek.examservice.repository;

import com.smartek.examservice.entity.ExamDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamDraftRepository extends JpaRepository<ExamDraft, Long> {
    List<ExamDraft> findByExamIdAndUserId(Long examId, Long userId);
    Optional<ExamDraft> findByExamIdAndUserIdAndQuestionId(Long examId, Long userId, Long questionId);
    void deleteByExamIdAndUserId(Long examId, Long userId);
}
