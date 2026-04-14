package com.smartek.offersservice.service;

import com.smartek.offersservice.dto.OfferRequest;
import com.smartek.offersservice.dto.OfferResponse;
import com.smartek.offersservice.dto.OfferSearchRequest;
import com.smartek.offersservice.dto.OfferStatsResponse;
import com.smartek.offersservice.entity.Offer;
import com.smartek.offersservice.exception.ResourceNotFoundException;
import com.smartek.offersservice.repository.ApplicationRepository;
import com.smartek.offersservice.repository.InterviewRepository;
import com.smartek.offersservice.repository.OfferRepository;
import com.smartek.offersservice.repository.SavedOfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfferService {

    private final OfferRepository offerRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;
    private final SavedOfferRepository savedOfferRepository;

    @Transactional
    public OfferResponse createOffer(OfferRequest request) {
        Offer offer = new Offer();
        offer.setTitle(request.getTitle());
        offer.setDescription(request.getDescription());
        offer.setCompanyName(request.getCompanyName());
        offer.setLocation(request.getLocation());
        offer.setContractType(request.getContractType());
        offer.setSalary(request.getSalary());
        offer.setSalaryMin(request.getSalaryMin());
        offer.setSalaryMax(request.getSalaryMax());
        offer.setDomain(request.getDomain());
        offer.setExperienceLevel(request.getExperienceLevel());
        offer.setRemote(request.getRemote() != null ? request.getRemote() : false);
        offer.setPositions(request.getPositions() != null ? request.getPositions() : 1);
        offer.setCompanyId(request.getCompanyId());
        offer.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");
        offer.setExpiresAt(request.getExpiresAt());
        return mapToResponse(offerRepository.save(offer));
    }

    public Page<OfferResponse> getAllOffers(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return offerRepository.findAll(pageable).map(this::mapToResponse);
    }

    public OfferResponse getOfferById(Long id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + id));
        // Incrémenter le compteur de vues
        offerRepository.incrementViewCount(id);
        offer.setViewCount(offer.getViewCount() + 1);
        return mapToResponse(offer);
    }

    public OfferResponse getOfferByIdWithCounts(Long id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + id));
        offerRepository.incrementViewCount(id);
        OfferResponse r = mapToResponse(offer);
        r.setApplicationCount(applicationRepository.countByOfferId(id));
        r.setSavedCount(savedOfferRepository.countByOfferId(id));
        return r;
    }

    public List<OfferResponse> getOffersByCompanyId(Long companyId) {
        return offerRepository.findByCompanyId(companyId).stream()
                .map(offer -> {
                    OfferResponse r = mapToResponse(offer);
                    r.setApplicationCount(applicationRepository.countByOfferId(offer.getId()));
                    return r;
                })
                .collect(Collectors.toList());
    }

    public Page<OfferResponse> getOffersByStatus(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return offerRepository.findByStatus(status, pageable).map(this::mapToResponse);
    }

    public Page<OfferResponse> searchOffers(OfferSearchRequest request) {
        Sort sort = request.getSortDir().equalsIgnoreCase("asc")
                ? Sort.by(request.getSortBy()).ascending()
                : Sort.by(request.getSortBy()).descending();
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        return offerRepository.searchWithFilters(
                request.getKeyword(),
                null,
                request.getContractType(),
                request.getLocation(),
                request.getDomain(),
                request.getExperienceLevel(),
                request.getRemote(),
                request.getSalaryMin(),
                request.getSalaryMax(),
                pageable
        ).map(this::mapToResponse);
    }

    public List<OfferResponse> getTopViewedOffers(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return offerRepository.findTopViewedOffers(pageable).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public OfferStatsResponse getStatsByCompany(Long companyId) {
        long total   = offerRepository.countByCompanyIdAndStatus(companyId, "ACTIVE")
                     + offerRepository.countByCompanyIdAndStatus(companyId, "CLOSED")
                     + offerRepository.countByCompanyIdAndStatus(companyId, "DRAFT")
                     + offerRepository.countByCompanyIdAndStatus(companyId, "EXPIRED");
        long active  = offerRepository.countByCompanyIdAndStatus(companyId, "ACTIVE");
        long closed  = offerRepository.countByCompanyIdAndStatus(companyId, "CLOSED");
        long draft   = offerRepository.countByCompanyIdAndStatus(companyId, "DRAFT");
        long expired = offerRepository.countByCompanyIdAndStatus(companyId, "EXPIRED");

        long totalApps   = applicationRepository.countByCompanyId(companyId);
        long pending     = applicationRepository.countByCompanyIdAndStatus(companyId, "PENDING");
        long accepted    = applicationRepository.countByCompanyIdAndStatus(companyId, "ACCEPTED");
        long rejected    = applicationRepository.countByCompanyIdAndStatus(companyId, "REJECTED");
        long totalInterv = interviewRepository.countByOffer_CompanyId(companyId);

        double rate = totalApps > 0 ? (double) accepted / totalApps * 100 : 0.0;

        return OfferStatsResponse.builder()
                .companyId(companyId)
                .totalOffers(total)
                .activeOffers(active)
                .closedOffers(closed)
                .draftOffers(draft)
                .expiredOffers(expired)
                .totalApplications(totalApps)
                .pendingApplications(pending)
                .acceptedApplications(accepted)
                .rejectedApplications(rejected)
                .totalInterviews(totalInterv)
                .acceptanceRate(rate)
                .build();
    }

    @Transactional
    public OfferResponse updateOffer(Long id, OfferRequest request) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + id));
        offer.setTitle(request.getTitle());
        offer.setDescription(request.getDescription());
        offer.setCompanyName(request.getCompanyName());
        offer.setLocation(request.getLocation());
        offer.setContractType(request.getContractType());
        offer.setSalary(request.getSalary());
        offer.setSalaryMin(request.getSalaryMin());
        offer.setSalaryMax(request.getSalaryMax());
        offer.setDomain(request.getDomain());
        offer.setExperienceLevel(request.getExperienceLevel());
        if (request.getRemote() != null) offer.setRemote(request.getRemote());
        if (request.getPositions() != null) offer.setPositions(request.getPositions());
        if (request.getStatus() != null) offer.setStatus(request.getStatus());
        if (request.getExpiresAt() != null) offer.setExpiresAt(request.getExpiresAt());
        return mapToResponse(offerRepository.save(offer));
    }

    @Transactional
    public void deleteOffer(Long id) {
        if (!offerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Offer not found with id: " + id);
        }
        offerRepository.deleteById(id);
    }

    private OfferResponse mapToResponse(Offer offer) {
        OfferResponse r = new OfferResponse();
        r.setId(offer.getId());
        r.setTitle(offer.getTitle());
        r.setDescription(offer.getDescription());
        r.setCompanyName(offer.getCompanyName());
        r.setLocation(offer.getLocation());
        r.setContractType(offer.getContractType());
        r.setSalary(offer.getSalary());
        r.setSalaryMin(offer.getSalaryMin());
        r.setSalaryMax(offer.getSalaryMax());
        r.setDomain(offer.getDomain());
        r.setExperienceLevel(offer.getExperienceLevel());
        r.setRemote(offer.getRemote());
        r.setPositions(offer.getPositions());
        r.setViewCount(offer.getViewCount());
        r.setCompanyId(offer.getCompanyId());
        r.setStatus(offer.getStatus());
        r.setExpiresAt(offer.getExpiresAt());
        r.setCreatedAt(offer.getCreatedAt());
        r.setUpdatedAt(offer.getUpdatedAt());
        return r;
    }
}
