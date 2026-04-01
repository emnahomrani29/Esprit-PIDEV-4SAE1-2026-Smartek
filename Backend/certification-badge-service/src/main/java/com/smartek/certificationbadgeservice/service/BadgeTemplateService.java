package com.smartek.certificationbadgeservice.service;

import com.smartek.certificationbadgeservice.dto.BadgeTemplateDTO;
import com.smartek.certificationbadgeservice.entity.BadgeTemplate;
import com.smartek.certificationbadgeservice.exception.ResourceNotFoundException;
import com.smartek.certificationbadgeservice.exception.ValidationException;
import com.smartek.certificationbadgeservice.mapper.BadgeTemplateMapper;
import com.smartek.certificationbadgeservice.repository.BadgeTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BadgeTemplateService {
    
    private final BadgeTemplateRepository badgeTemplateRepository;
    private final BadgeTemplateMapper badgeTemplateMapper;
    
    @Transactional
    public BadgeTemplateDTO create(BadgeTemplateDTO dto) {
        MDC.put("operation", "CREATE_BADGE_TEMPLATE");
        try {
            log.info("Creating badge template with name: {}", dto.getName());
            validateBadgeTemplate(dto);
            
            BadgeTemplate entity = badgeTemplateMapper.toEntity(dto);
            BadgeTemplate saved = badgeTemplateRepository.save(entity);
            
            log.info("Successfully created badge template with id: {} and name: {}", saved.getId(), saved.getName());
            return badgeTemplateMapper.toDTO(saved);
        } catch (ValidationException e) {
            log.warn("Validation error while creating badge template: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating badge template", e);
            throw e;
        } finally {
            MDC.remove("operation");
        }
    }
    
    @Transactional
    public BadgeTemplateDTO update(Long id, BadgeTemplateDTO dto) {
        MDC.put("operation", "UPDATE_BADGE_TEMPLATE");
        try {
            log.info("Updating badge template with id: {}", id);
            validateBadgeTemplate(dto);
            
            BadgeTemplate entity = badgeTemplateRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Badge template not found with id: {}", id);
                        return new ResourceNotFoundException("Badge template not found with id: " + id);
                    });
            
            badgeTemplateMapper.updateEntityFromDTO(dto, entity);
            BadgeTemplate updated = badgeTemplateRepository.save(entity);
            
            log.info("Successfully updated badge template with id: {}", id);
            return badgeTemplateMapper.toDTO(updated);
        } catch (ValidationException e) {
            log.warn("Validation error while updating badge template with id {}: {}", id, e.getMessage());
            throw e;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating badge template with id: {}", id, e);
            throw e;
        } finally {
            MDC.remove("operation");
        }
    }
    
    @Transactional(readOnly = true)
    public List<BadgeTemplateDTO> findAll() {
        MDC.put("operation", "FIND_ALL_BADGE_TEMPLATES");
        try {
            log.info("Retrieving all badge templates");
            List<BadgeTemplateDTO> templates = badgeTemplateRepository.findAll().stream()
                    .map(badgeTemplateMapper::toDTO)
                    .collect(Collectors.toList());
            log.info("Successfully retrieved {} badge templates", templates.size());
            return templates;
        } catch (Exception e) {
            log.error("Error retrieving all badge templates", e);
            throw e;
        } finally {
            MDC.remove("operation");
        }
    }
    
    @Transactional(readOnly = true)
    public Page<BadgeTemplateDTO> findAllPaginated(Pageable pageable) {
        MDC.put("operation", "FIND_ALL_BADGE_TEMPLATES_PAGINATED");
        try {
            log.info("Retrieving badge templates with pagination - page: {}, size: {}", 
                    pageable.getPageNumber(), pageable.getPageSize());
            Page<BadgeTemplate> page = badgeTemplateRepository.findAll(pageable);
            Page<BadgeTemplateDTO> dtoPage = page.map(badgeTemplateMapper::toDTO);
            log.info("Successfully retrieved page {} with {} badge templates", 
                    pageable.getPageNumber(), dtoPage.getNumberOfElements());
            return dtoPage;
        } catch (Exception e) {
            log.error("Error retrieving paginated badge templates", e);
            throw e;
        } finally {
            MDC.remove("operation");
        }
    }

    
    @Transactional(readOnly = true)
    public BadgeTemplateDTO findById(Long id) {
        MDC.put("operation", "FIND_BADGE_TEMPLATE_BY_ID");
        try {
            log.info("Retrieving badge template with id: {}", id);
            BadgeTemplate entity = badgeTemplateRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Badge template not found with id: {}", id);
                        return new ResourceNotFoundException("Badge template not found with id: " + id);
                    });
            
            log.info("Successfully retrieved badge template with id: {}", id);
            return badgeTemplateMapper.toDTO(entity);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving badge template with id: {}", id, e);
            throw e;
        } finally {
            MDC.remove("operation");
        }
    }
    
    @Transactional
    public void delete(Long id) {
        MDC.put("operation", "DELETE_BADGE_TEMPLATE");
        try {
            log.info("Deleting badge template with id: {}", id);
            BadgeTemplate entity = badgeTemplateRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Badge template not found with id: {}", id);
                        return new ResourceNotFoundException("Badge template not found with id: " + id);
                    });
            
            // Delete only the template, earned badges are preserved due to cascade settings
            badgeTemplateRepository.delete(entity);
            log.info("Successfully deleted badge template with id: {}", id);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting badge template with id: {}", id, e);
            throw e;
        } finally {
            MDC.remove("operation");
        }
    }
    
    private void validateBadgeTemplate(BadgeTemplateDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new ValidationException("Badge name is required");
        }
        
        if (dto.getName().length() > 100) {
            throw new ValidationException("Badge name must not exceed 100 characters");
        }
        
        if (dto.getDescription() != null && dto.getDescription().length() > 1000) {
            throw new ValidationException("Description must not exceed 1000 characters");
        }
    }
}
