package com.smartek.sponsor.controller;

import com.smartek.sponsor.dto.SponsorDashboardDTO;
import com.smartek.sponsor.entity.Sponsor;
import com.smartek.sponsor.service.SponsorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sponsors")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SponsorController {
    private final SponsorService sponsorService;

    @PostMapping
    public ResponseEntity<Sponsor> createSponsor(@RequestBody Sponsor sponsor) {
        Sponsor created = sponsorService.createSponsor(sponsor);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Sponsor>> getAllSponsors() {
        return ResponseEntity.ok(sponsorService.getAllSponsors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sponsor> getSponsorById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(sponsorService.getSponsorById(id));
    }

    @GetMapping("/by-email")
    public ResponseEntity<Sponsor> getSponsorByEmail(@RequestParam("email") String email) {
        return ResponseEntity.ok(sponsorService.getSponsorByEmail(email));
    }

    @GetMapping("/{id}/dashboard")
    public ResponseEntity<SponsorDashboardDTO> getSponsorDashboard(@PathVariable("id") Long id) {
        return ResponseEntity.ok(sponsorService.getSponsorDashboard(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sponsor> updateSponsor(@PathVariable("id") Long id, @RequestBody Sponsor sponsor) {
        return ResponseEntity.ok(sponsorService.updateSponsor(id, sponsor));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSponsor(@PathVariable("id") Long id) {
        sponsorService.deleteSponsor(id);
        return ResponseEntity.noContent().build();
    }
}
