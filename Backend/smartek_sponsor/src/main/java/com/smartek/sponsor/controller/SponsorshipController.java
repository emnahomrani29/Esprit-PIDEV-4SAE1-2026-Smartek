package com.smartek.sponsor.controller;

import com.smartek.sponsor.dto.CreateSponsorshipRequest;
import com.smartek.sponsor.dto.SponsorshipResponseDTO;
import com.smartek.sponsor.entity.Sponsorship;
import com.smartek.sponsor.mapper.SponsorshipMapper;
import com.smartek.sponsor.service.SponsorshipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/sponsorships")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SponsorshipController {
    
    private final SponsorshipService sponsorshipService;
    private final SponsorshipMapper sponsorshipMapper;

    /**
     * Create a new sponsorship with validations
     */
    @PostMapping
    public ResponseEntity<SponsorshipResponseDTO> createSponsorship(
            @Valid @RequestBody CreateSponsorshipRequest request) {
        Sponsorship sponsorship = sponsorshipMapper.toEntity(request);
        Sponsorship created = sponsorshipService.createSponsorship(request.getContractId(), sponsorship);
        return ResponseEntity.status(HttpStatus.CREATED).body(sponsorshipMapper.toResponseDTO(created));
    }

    /**
     * Get all sponsorships
     */
    @GetMapping
    public ResponseEntity<List<SponsorshipResponseDTO>> getAllSponsorships() {
        List<Sponsorship> sponsorships = sponsorshipService.getAllSponsorships();
        List<SponsorshipResponseDTO> dtos = sponsorships.stream()
                .map(sponsorshipMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get sponsorship by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<SponsorshipResponseDTO> getSponsorshipById(@PathVariable("id") Long id) {
        Sponsorship sponsorship = sponsorshipService.getSponsorshipById(id);
        return ResponseEntity.ok(sponsorshipMapper.toResponseDTO(sponsorship));
    }

    /**
     * Update sponsorship (only PENDING or REJECTED can be updated)
     */
    @PutMapping("/{id}")
    public ResponseEntity<SponsorshipResponseDTO> updateSponsorship(
            @PathVariable("id") Long id,
            @RequestParam(value = "contractId", required = false) Long contractId,
            @Valid @RequestBody CreateSponsorshipRequest request) {
        Sponsorship sponsorship = sponsorshipMapper.toEntity(request);
        Sponsorship updated = sponsorshipService.updateSponsorship(id, contractId, sponsorship);
        return ResponseEntity.ok(sponsorshipMapper.toResponseDTO(updated));
    }

    /**
     * Delete sponsorship (only PENDING, REJECTED, or CANCELLED)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSponsorship(@PathVariable("id") Long id) {
        sponsorshipService.deleteSponsorship(id);
        return ResponseEntity.noContent().build();
    }
}

