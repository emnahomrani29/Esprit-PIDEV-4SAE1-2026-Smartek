package com.smartek.sponsor.service;

import com.smartek.sponsor.dto.SponsorDashboardDTO;
import com.smartek.sponsor.entity.Sponsor;

import java.util.List;

public interface SponsorService {
    Sponsor createSponsor(Sponsor sponsor);
    List<Sponsor> getAllSponsors();
    Sponsor getSponsorById(Long sponsorId);
    Sponsor getSponsorByEmail(String email);
    Sponsor updateSponsor(Long sponsorId, Sponsor sponsor);
    void deleteSponsor(Long sponsorId);
    SponsorDashboardDTO getSponsorDashboard(Long sponsorId);
}

