package com.smartek.certificationbadgeservice.repository;

import com.smartek.certificationbadgeservice.entity.CertificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CertificationTemplateRepository extends JpaRepository<CertificationTemplate, Long>, PagingAndSortingRepository<CertificationTemplate, Long> {

    /**
     * Find certification template by exam ID.
     * Used for auto-award system to determine which certification to award based on exam results.
     */
    Optional<CertificationTemplate> findByExamId(Long examId);
}

