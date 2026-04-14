package com.smartek.certificationbadgeservice.controller;

import com.smartek.certificationbadgeservice.dto.CertificationTemplateDTO;
import com.smartek.certificationbadgeservice.service.CertificationTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing certification templates.
 * Provides endpoints for CRUD operations on certification templates.
 */
@RestController
@RequestMapping("/api/certifications-badges/certification-templates")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
@Slf4j
public class CertificationTemplateController {
    
    private final CertificationTemplateService certificationTemplateService;
    
    /**
     * Create a new certification template.
     * Only accessible by TRAINER and ADMIN roles.
     */
    @PostMapping
    // @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')") // Temporarily disabled for testing
    public ResponseEntity<CertificationTemplateDTO> createCertificationTemplate(
            @Valid @RequestBody CertificationTemplateDTO certificationTemplateDTO) {
        log.info("Creating certification template with title: {}", certificationTemplateDTO.getTitle());
        CertificationTemplateDTO created = certificationTemplateService.create(certificationTemplateDTO);
        log.info("Certification template created with id: {}", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    /**
     * Update an existing certification template.
     * Only accessible by TRAINER and ADMIN roles.
     */
    @PutMapping("/{id}")
    // @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')") // Temporarily disabled for testing
    public ResponseEntity<CertificationTemplateDTO> updateCertificationTemplate(
            @PathVariable Long id,
            @Valid @RequestBody CertificationTemplateDTO certificationTemplateDTO) {
        log.info("Updating certification template with id: {}", id);
        CertificationTemplateDTO updated = certificationTemplateService.update(id, certificationTemplateDTO);
        log.info("Certification template updated with id: {}", id);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Get all certification templates.
     * Accessible by all authenticated users.
     */
    @GetMapping
    public ResponseEntity<List<CertificationTemplateDTO>> getAllCertificationTemplates() {
        log.info("Retrieving all certification templates");
        List<CertificationTemplateDTO> templates = certificationTemplateService.findAll();
        log.info("Retrieved {} certification templates", templates.size());
        return ResponseEntity.ok(templates);
    }
    
    /**
     * Get certification templates with pagination.
     * Accessible by all authenticated users.
     */
    @GetMapping("/paginated")
    public ResponseEntity<Page<CertificationTemplateDTO>> getCertificationTemplatesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("Retrieving certification templates - page: {}, size: {}, sortBy: {}, direction: {}", 
                page, size, sortBy, sortDirection);
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<CertificationTemplateDTO> templates = certificationTemplateService.findAllPaginated(pageable);
        log.info("Retrieved page {} of certification templates with {} items", page, templates.getNumberOfElements());
        return ResponseEntity.ok(templates);
    }
    
    /**
     * Get a specific certification template by ID.
     * Accessible by all authenticated users.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CertificationTemplateDTO> getCertificationTemplateById(@PathVariable Long id) {
        log.info("Retrieving certification template with id: {}", id);
        CertificationTemplateDTO template = certificationTemplateService.findById(id);
        return ResponseEntity.ok(template);
    }
    
    /**
     * Delete a certification template.
     * Only accessible by TRAINER and ADMIN roles.
     * Earned certifications are preserved.
     */
    @DeleteMapping("/{id}")
    // @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')") // Temporarily disabled for testing
    public ResponseEntity<Void> deleteCertificationTemplate(@PathVariable Long id) {
        log.info("Deleting certification template with id: {}", id);
        certificationTemplateService.delete(id);
        log.info("Certification template deleted with id: {}", id);
        return ResponseEntity.noContent().build();
    }
}
