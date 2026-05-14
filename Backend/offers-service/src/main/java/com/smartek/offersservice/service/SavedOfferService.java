package com.smartek.offersservice.service;

import com.smartek.offersservice.dto.OfferResponse;
import com.smartek.offersservice.entity.Offer;
import com.smartek.offersservice.entity.SavedOffer;
import com.smartek.offersservice.exception.BusinessException;
import com.smartek.offersservice.exception.ResourceNotFoundException;
import com.smartek.offersservice.repository.OfferRepository;
import com.smartek.offersservice.repository.SavedOfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavedOfferService {

    private final SavedOfferRepository savedOfferRepository;
    private final OfferRepository offerRepository;

    @Transactional
    public void saveOffer(Long offerId, Long learnerId) {
        if (!offerRepository.existsById(offerId)) {
            throw new ResourceNotFoundException("Offre non trouvée avec l'id: " + offerId);
        }
        if (savedOfferRepository.existsByOfferIdAndLearnerId(offerId, learnerId)) {
            throw new BusinessException("Cette offre est déjà dans vos favoris");
        }
        SavedOffer saved = new SavedOffer();
        saved.setOfferId(offerId);
        saved.setLearnerId(learnerId);
        savedOfferRepository.save(saved);
        log.info("Learner {} saved offer {}", learnerId, offerId);
    }

    @Transactional
    public void unsaveOffer(Long offerId, Long learnerId) {
        if (!savedOfferRepository.existsByOfferIdAndLearnerId(offerId, learnerId)) {
            throw new ResourceNotFoundException("Favori non trouvé");
        }
        savedOfferRepository.deleteByOfferIdAndLearnerId(offerId, learnerId);
        log.info("Learner {} unsaved offer {}", learnerId, offerId);
    }

    public List<OfferResponse> getSavedOffersByLearner(Long learnerId) {
        return savedOfferRepository.findByLearnerId(learnerId).stream()
                .map(saved -> offerRepository.findById(saved.getOfferId()))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public boolean isSaved(Long offerId, Long learnerId) {
        return savedOfferRepository.existsByOfferIdAndLearnerId(offerId, learnerId);
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
        r.setDomain(offer.getDomain());
        r.setExperienceLevel(offer.getExperienceLevel());
        r.setRemote(offer.getRemote());
        r.setCompanyId(offer.getCompanyId());
        r.setStatus(offer.getStatus());
        r.setExpiresAt(offer.getExpiresAt());
        r.setCreatedAt(offer.getCreatedAt());
        r.setRequiredSkills(offer.getRequiredSkills());
        r.setOpen(offer.isOpen());
        return r;
    }
}
