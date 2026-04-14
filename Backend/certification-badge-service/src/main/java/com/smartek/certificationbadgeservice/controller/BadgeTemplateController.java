package com.smartek.certificationbadgeservice.controller;

import com.smartek.certificationbadgeservice.dto.BadgeTemplateDTO;
import com.smartek.certificationbadgeservice.service.BadgeTemplateService;
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
 * REST controller for managing badge templates.
 * Provides endpoints for CRUD operations on badge templates.
 */
@RestController
@RequestMapping("/api/certifications-badges/badge-templates")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
@Slf4j
public class BadgeTemplateController {
    
    private final BadgeTemplateService badgeTemplateService;
    
    /**
     * Create a new badge template.
     * Only accessible by TRAINER and ADMIN roles.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    public ResponseEntity<BadgeTemplateDTO> createBadgeTemplate(@Valid @RequestBody BadgeTemplateDTO badgeTemplateDTO) {
        log.info("Creating badge template with name: {}", badgeTemplateDTO.getName());
        BadgeTemplateDTO created = badgeTemplateService.create(badgeTemplateDTO);
        log.info("Badge template created with id: {}", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    /**
     * Update an existing badge template.
     * Only accessible by TRAINER and ADMIN roles.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    public ResponseEntity<BadgeTemplateDTO> updateBadgeTemplate(
            @PathVariable Long id,
            @Valid @RequestBody BadgeTemplateDTO badgeTemplateDTO) {
        log.info("Updating badge template with id: {}", id);
        BadgeTemplateDTO updated = badgeTemplateService.update(id, badgeTemplateDTO);
        log.info("Badge template updated with id: {}", id);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Get all badge templates.
     * Accessible by all authenticated users.
     */
    @GetMapping
    public ResponseEntity<List<BadgeTemplateDTO>> getAllBadgeTemplates() {
        log.info("Retrieving all badge templates");
        List<BadgeTemplateDTO> templates = badgeTemplateService.findAll();
        log.info("Retrieved {} badge templates", templates.size());
        return ResponseEntity.ok(templates);
    }
    
    /**
     * Get badge templates with pagination.
     * Accessible by all authenticated users.
     */
    @GetMapping("/paginated")
    public ResponseEntity<Page<BadgeTemplateDTO>> getBadgeTemplatesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("Retrieving badge templates - page: {}, size: {}, sortBy: {}, direction: {}", 
                page, size, sortBy, sortDirection);
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<BadgeTemplateDTO> templates = badgeTemplateService.findAllPaginated(pageable);
        log.info("Retrieved page {} of badge templates with {} items", page, templates.getNumberOfElements());
        return ResponseEntity.ok(templates);
    }
    
    /**
     * Get a specific badge template by ID.
     * Accessible by all authenticated users.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BadgeTemplateDTO> getBadgeTemplateById(@PathVariable Long id) {
        log.info("Retrieving badge template with id: {}", id);
        BadgeTemplateDTO template = badgeTemplateService.findById(id);
        return ResponseEntity.ok(template);
    }
    
    /**
     * Delete a badge template.
     * Only accessible by TRAINER and ADMIN roles.
     * Earned badges are preserved.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    public ResponseEntity<Void> deleteBadgeTemplate(@PathVariable Long id) {
        log.info("Deleting badge template with id: {}", id);
        badgeTemplateService.delete(id);
        log.info("Badge template deleted with id: {}", id);
        return ResponseEntity.noContent().build();
    }
}
