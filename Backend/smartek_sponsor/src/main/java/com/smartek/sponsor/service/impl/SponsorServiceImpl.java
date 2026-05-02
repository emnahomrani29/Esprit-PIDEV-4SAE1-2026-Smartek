package com.smartek.sponsor.service.impl;

import com.smartek.sponsor.dto.AuthRegisterRequest;
import com.smartek.sponsor.dto.SponsorDashboardDTO;
import com.smartek.sponsor.entity.Contract;
import com.smartek.sponsor.entity.Sponsor;
import com.smartek.sponsor.entity.Sponsorship;
import com.smartek.sponsor.enums.RoleType;
import com.smartek.sponsor.exception.ResourceNotFoundException;
import com.smartek.sponsor.repository.ContractRepository;
import com.smartek.sponsor.repository.SponsorRepository;
import com.smartek.sponsor.repository.SponsorshipRepository;
import com.smartek.sponsor.service.SponsorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
public class SponsorServiceImpl implements SponsorService {
    private final SponsorRepository sponsorRepository;
    private final ContractRepository contractRepository;
    private final SponsorshipRepository sponsorshipRepository;
    private final RestTemplate restTemplate;

    @Value("${auth.service.url:http://localhost:8081}")
    private String authServiceUrl;

    public SponsorServiceImpl(SponsorRepository sponsorRepository,
                              ContractRepository contractRepository,
                              SponsorshipRepository sponsorshipRepository,
                              RestTemplate restTemplate) {
        this.sponsorRepository = sponsorRepository;
        this.contractRepository = contractRepository;
        this.sponsorshipRepository = sponsorshipRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public Sponsor createSponsor(Sponsor sponsor) {
        // Register the sponsor as a user in the auth-service
        registerSponsorInAuthService(sponsor);

        // Save sponsor in sponsor database (password is @Transient, not saved)
        return sponsorRepository.save(sponsor);
    }

    private void registerSponsorInAuthService(Sponsor sponsor) {
        try {
            AuthRegisterRequest authRequest = AuthRegisterRequest.builder()
                    .firstName(sponsor.getName())
                    .email(sponsor.getEmail())
                    .password(sponsor.getPassword())
                    .phone(sponsor.getPhone())
                    .experience(0)
                    .role(RoleType.SPONSOR)
                    .build();

            ResponseEntity<Object> response = restTemplate.postForEntity(
                    authServiceUrl + "/api/auth/register",
                    authRequest,
                    Object.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Sponsor registered in auth-service: {}", sponsor.getEmail());
            } else {
                log.error("Failed to register sponsor in auth-service. Status: {}", response.getStatusCode());
                throw new RuntimeException("Failed to create sponsor account");
            }
        } catch (Exception e) {
            log.error("Failed to register sponsor in auth-service: {}", e.getMessage());
            throw new RuntimeException("Failed to create sponsor account: " + e.getMessage());
        }
    }

    @Override
    public List<Sponsor> getAllSponsors() {
        return sponsorRepository.findAll();
    }

    @Override
    public Sponsor getSponsorById(Long sponsorId) {
        return sponsorRepository.findById(sponsorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sponsor", "id", sponsorId));
    }

    @Override
    public Sponsor getSponsorByEmail(String email) {
        return sponsorRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Sponsor", "email", email));
    }

    @Override
    public Sponsor updateSponsor(Long sponsorId, Sponsor sponsor) {
        Sponsor existing = getSponsorById(sponsorId);
        existing.setName(sponsor.getName());
        existing.setEmail(sponsor.getEmail());
        existing.setPhone(sponsor.getPhone());
        existing.setCompanyName(sponsor.getCompanyName());
        existing.setIndustry(sponsor.getIndustry());
        existing.setWebsite(sponsor.getWebsite());
        existing.setLogoUrl(sponsor.getLogoUrl());
        existing.setStatus(sponsor.getStatus());
        return sponsorRepository.save(existing);
    }

    @Override
    public void deleteSponsor(Long sponsorId) {
        Sponsor existing = getSponsorById(sponsorId);
        sponsorRepository.delete(existing);
    }

    @Override
    public SponsorDashboardDTO getSponsorDashboard(Long sponsorId) {
        Sponsor sponsor = getSponsorById(sponsorId);
        List<Contract> contracts = contractRepository.findBySponsorId(sponsorId);
        List<Sponsorship> sponsorships = sponsorshipRepository.findByContractSponsorId(sponsorId);

        Double totalContractAmount = contracts.stream()
                .filter(c -> c.getAmount() != null)
                .mapToDouble(Contract::getAmount)
                .sum();

        Double totalSpent = sponsorships.stream()
                .filter(s -> s.getAmountAllocated() != null)
                .mapToDouble(Sponsorship::getAmountAllocated)
                .sum();

        Double remainingBalance = totalContractAmount - totalSpent;

        return new SponsorDashboardDTO(sponsor, contracts, sponsorships, totalContractAmount, totalSpent, remainingBalance);
    }
}
