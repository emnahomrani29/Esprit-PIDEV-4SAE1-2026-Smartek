package com.smartek.sponsor.service;

import com.smartek.sponsor.dto.PlatformStatsDTO;
import com.smartek.sponsor.dto.TopSponsorDTO;

import java.util.List;

public interface AdminService {
    PlatformStatsDTO getPlatformStats();
    List<TopSponsorDTO> getTopSponsors();
}