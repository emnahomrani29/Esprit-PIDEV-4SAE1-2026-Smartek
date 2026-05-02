package com.smartek.sponsor.controller;

import com.smartek.sponsor.dto.PlatformStatsDTO;
import com.smartek.sponsor.dto.TopSponsorDTO;
import com.smartek.sponsor.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/platform-stats")
    public ResponseEntity<PlatformStatsDTO> getPlatformStats() {
        try {
            PlatformStatsDTO stats = adminService.getPlatformStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("Error getting platform stats: " + e.getMessage());
            e.printStackTrace();
            // Return empty stats on error - no fake data
            PlatformStatsDTO emptyStats = new PlatformStatsDTO();
            emptyStats.setTotalBudget(0.0);
            emptyStats.setTotalSponsors(0L);
            emptyStats.setTotalSponsorships(0L);
            emptyStats.setPendingApprovals(0L);
            emptyStats.setBudgetGrowth(0.0);
            emptyStats.setSponsorGrowth(0.0);
            emptyStats.setSponsorshipGrowth(0.0);
            emptyStats.setApprovalRate(0.0);
            emptyStats.setAvgResponseTime(0.0);
            emptyStats.setActiveContracts(0L);
            return ResponseEntity.ok(emptyStats);
        }
    }

    @GetMapping("/top-sponsors")
    public ResponseEntity<List<TopSponsorDTO>> getTopSponsors() {
        try {
            List<TopSponsorDTO> topSponsors = adminService.getTopSponsors();
            return ResponseEntity.ok(topSponsors);
        } catch (Exception e) {
            // Return empty list on error
            return ResponseEntity.ok(List.of());
        }
    }
}