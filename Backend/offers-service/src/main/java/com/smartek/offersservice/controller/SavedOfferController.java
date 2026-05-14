package com.smartek.offersservice.controller;

import com.smartek.offersservice.dto.OfferResponse;
import com.smartek.offersservice.service.SavedOfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/saved-offers")
@RequiredArgsConstructor
public class SavedOfferController {

    private final SavedOfferService savedOfferService;

    // Sauvegarder une offre en favori
    @PostMapping("/{offerId}/learner/{learnerId}")
    public ResponseEntity<Void> saveOffer(@PathVariable Long offerId, @PathVariable Long learnerId) {
        savedOfferService.saveOffer(offerId, learnerId);
        return ResponseEntity.ok().build();
    }

    // Retirer des favoris
    @DeleteMapping("/{offerId}/learner/{learnerId}")
    public ResponseEntity<Void> unsaveOffer(@PathVariable Long offerId, @PathVariable Long learnerId) {
        savedOfferService.unsaveOffer(offerId, learnerId);
        return ResponseEntity.noContent().build();
    }

    // Lister les favoris d'un learner
    @GetMapping("/learner/{learnerId}")
    public ResponseEntity<List<OfferResponse>> getSavedOffers(@PathVariable Long learnerId) {
        return ResponseEntity.ok(savedOfferService.getSavedOffersByLearner(learnerId));
    }

    // Vérifier si une offre est en favori
    @GetMapping("/{offerId}/learner/{learnerId}/check")
    public ResponseEntity<Boolean> isSaved(@PathVariable Long offerId, @PathVariable Long learnerId) {
        return ResponseEntity.ok(savedOfferService.isSaved(offerId, learnerId));
    }
}
