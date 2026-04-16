package com.smartek.offersservice.service;

import com.smartek.offersservice.dto.OfferRequest;
import com.smartek.offersservice.dto.OfferResponse;
import com.smartek.offersservice.dto.OfferSearchRequest;
import com.smartek.offersservice.dto.OfferStatsResponse;
import com.smartek.offersservice.entity.Offer;
import com.smartek.offersservice.entity.Application;
import com.smartek.offersservice.exception.BusinessException;
import com.smartek.offersservice.exception.ResourceNotFoundException;
import com.smartek.offersservice.mapper.OfferMapper;
import com.smartek.offersservice.repository.ApplicationRepository;
import com.smartek.offersservice.repository.InterviewRepository;
import com.smartek.offersservice.repository.OfferRepository;
import com.smartek.offersservice.repository.SavedOfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final OfferMapper offerMapper;

    @Transactional
    public OfferResponse createOffer(OfferRequest request) {
        // Validate expiry date
        if (request.getExpiresAt() != null && request.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("La date d'expiration ne peut pas être dans le passé");
        }
        Offer offer = offerMapper.toEntity(request);
        return offerMapper.toResponse(offerRepository.save(offer));
    }

    public Page<OfferResponse> getAllOffers(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return offerRepository.findAll(pageable).map(offerMapper::toResponse);
    }

    public OfferResponse getOfferById(Long id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + id));
        offerRepository.incrementViewCount(id);
        offer.setViewCount(offer.getViewCount() + 1);
        return offerMapper.toResponse(offer);
    }

    public OfferResponse getOfferByIdWithCounts(Long id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + id));
        offerRepository.incrementViewCount(id);
        OfferResponse r = offerMapper.toResponse(offer);
        r.setApplicationCount(applicationRepository.countByOfferId(id));
        r.setSavedCount(savedOfferRepository.countByOfferId(id));
        return r;
    }

    public List<OfferResponse> getOffersByCompanyId(Long companyId) {
        return offerRepository.findByCompanyId(companyId).stream()
                .map(offer -> {
                    OfferResponse r = offerMapper.toResponse(offer);
                    r.setApplicationCount(applicationRepository.countByOfferId(offer.getId()));
                    return r;
                })
                .collect(Collectors.toList());
    }

    public List<OfferResponse> getOffersByStatus(String status) {
        return offerRepository.findByStatus(Offer.OfferStatus.valueOf(status)).stream()
                .map(offerMapper::toResponse)
                .collect(Collectors.toList());
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
                request.getExperienceLevel() != null ? request.getExperienceLevel().name() : null,
                request.getRemote(),
                request.getSalaryMin(),
                request.getSalaryMax(),
                pageable
        ).map(offerMapper::toResponse);
    }

    public List<OfferResponse> getTopViewedOffers(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return offerRepository.findTopViewedOffers(pageable).stream()
                .map(offerMapper::toResponse)
                .collect(Collectors.toList());
    }

    public OfferStatsResponse getStatsByCompany(Long companyId) {
        long active  = offerRepository.countByCompanyIdAndStatus(companyId, Offer.OfferStatus.ACTIVE);
        long closed  = offerRepository.countByCompanyIdAndStatus(companyId, Offer.OfferStatus.CLOSED);
        long draft   = offerRepository.countByCompanyIdAndStatus(companyId, Offer.OfferStatus.DRAFT);
        long expired = offerRepository.countByCompanyIdAndStatus(companyId, Offer.OfferStatus.EXPIRED);
        long total   = active + closed + draft + expired;

        long totalApps = applicationRepository.countByCompanyId(companyId);
        long pending   = applicationRepository.countByCompanyIdAndStatus(companyId, Application.ApplicationStatus.PENDING);
        long accepted  = applicationRepository.countByCompanyIdAndStatus(companyId, Application.ApplicationStatus.ACCEPTED);
        long rejected  = applicationRepository.countByCompanyIdAndStatus(companyId, Application.ApplicationStatus.REJECTED);
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
        offerMapper.updateEntityFromRequest(offer, request);
        return offerMapper.toResponse(offerRepository.save(offer));
    }

    @Transactional
    public void deleteOffer(Long id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + id));
        // Check no accepted applications
        long accepted = applicationRepository.countByOfferIdAndStatus(offer.getId(), Application.ApplicationStatus.ACCEPTED);
        if (accepted > 0) {
            throw new BusinessException("Impossible de supprimer une offre avec des candidatures acceptées");
        }
        offerRepository.deleteById(id);
    }
}
