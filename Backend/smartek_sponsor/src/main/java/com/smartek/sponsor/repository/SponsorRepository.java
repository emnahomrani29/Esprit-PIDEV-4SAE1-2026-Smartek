package com.smartek.sponsor.repository;

import com.smartek.sponsor.entity.Sponsor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SponsorRepository extends JpaRepository<Sponsor, Long> {
    Optional<Sponsor> findByEmail(String email);
    boolean existsByEmail(String email);
}

