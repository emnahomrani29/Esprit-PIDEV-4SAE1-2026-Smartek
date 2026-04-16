package com.smartek.offersservice.controller;

import com.smartek.offersservice.dto.*;
import com.smartek.offersservice.service.OfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;

    @PostMapping
    public ResponseEntity<OfferResponse> createOffer(@Valid @RequestBody OfferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(offerService.createOffer(request));
    }

    // Pagination : GET /api/offers?page=0&size=10&sortBy=createdAt&sortDir=desc
    @GetMapping
    public ResponseEntity<Page<OfferResponse>> getAllOffers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(offerService.getAllOffers(page, size, sortBy, sortDir));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfferResponse> getOfferById(@PathVariable Long id) {
        return ResponseEntity.ok(offerService.getOfferById(id));
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<OfferResponse> getOfferDetails(@PathVariable Long id) {
        return ResponseEntity.ok(offerService.getOfferByIdWithCounts(id));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<OfferResponse>> getOffersByCompanyId(@PathVariable Long companyId) {
        return ResponseEntity.ok(offerService.getOffersByCompanyId(companyId));
    }

    // Endpoint qui utilise le X-User-Id du token JWT (injecté par l'API Gateway)
    @GetMapping("/my")
    public ResponseEntity<List<OfferResponse>> getMyOffers(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (userId == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(offerService.getOffersByCompanyId(userId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OfferResponse>> getOffersByStatus(@PathVariable String status) {
        return ResponseEntity.ok(offerService.getOffersByStatus(status));
    }

    // Recherche avancée : POST /api/offers/search
    @PostMapping("/search")
    public ResponseEntity<Page<OfferResponse>> searchOffers(@RequestBody OfferSearchRequest request) {
        return ResponseEntity.ok(offerService.searchOffers(request));
    }

    // Top offres les plus vues : GET /api/offers/top-viewed?limit=5
    @GetMapping("/top-viewed")
    public ResponseEntity<List<OfferResponse>> getTopViewedOffers(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(offerService.getTopViewedOffers(limit));
    }

    // Statistiques entreprise : GET /api/offers/stats/company/{companyId}
    @GetMapping("/stats/company/{companyId}")
    public ResponseEntity<OfferStatsResponse> getStatsByCompany(@PathVariable Long companyId) {
        return ResponseEntity.ok(offerService.getStatsByCompany(companyId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OfferResponse> updateOffer(
            @PathVariable Long id,
            @Valid @RequestBody OfferRequest request) {
        return ResponseEntity.ok(offerService.updateOffer(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {
        offerService.deleteOffer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Offers Service is running!");
    }
}
