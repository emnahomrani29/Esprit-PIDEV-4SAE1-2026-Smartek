package com.smartek.sponsor.service;

import com.smartek.sponsor.entity.Sponsorship;

import java.util.List;

public interface SponsorshipService {
    Sponsorship createSponsorship(Long contractId, Sponsorship sponsorship);
    List<Sponsorship> getAllSponsorships();
    Sponsorship getSponsorshipById(Long sponsorshipId);
    Sponsorship updateSponsorship(Long sponsorshipId, Long contractId, Sponsorship sponsorship);
    void deleteSponsorship(Long sponsorshipId);
}

