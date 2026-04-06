package com.smartek.skillevidenceservice.repository;

import com.smartek.skillevidenceservice.entity.EvidenceCategory;
import com.smartek.skillevidenceservice.entity.EvidenceStatus;
import com.smartek.skillevidenceservice.entity.SkillEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillEvidenceRepository extends JpaRepository<SkillEvidence, Integer> {

    List<SkillEvidence> findByLearnerIdOrderByUploadDateDesc(Long learnerId);

    boolean existsByLearnerIdAndTitle(Long learnerId, String title);

    boolean existsByLearnerIdAndTitleAndEvidenceIdNot(Long learnerId, String title, Integer evidenceId);

    // New query methods for filtering
    List<SkillEvidence> findByStatus(EvidenceStatus status);

    List<SkillEvidence> findByCategory(EvidenceCategory category);
}