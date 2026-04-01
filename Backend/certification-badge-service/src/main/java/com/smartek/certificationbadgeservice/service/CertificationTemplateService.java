package com.smartek.certificationbadgeservice.service;

import com.smartek.certificationbadgeservice.dto.CertificationTemplateDTO;
import com.smartek.certificationbadgeservice.entity.CertificationTemplate;
import com.smartek.certificationbadgeservice.exception.ResourceNotFoundException;
import com.smartek.certificationbadgeservice.exception.ValidationException;
import com.smartek.certificationbadgeservice.mapper.CertificationTemplateMapper;
import com.smartek.certificationbadgeservice.repository.CertificationTemplateRepository;
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
public class CertificationTemplateService {
    
    private final CertificationTemplateRepository certificationTemplateRepository;
    private final CertificationTemplateMapper certificationTemplateMapper;
    
    @Transactional
    public CertificationTemplateDTO create(CertificationTemplateDTO dto) {
        MDC.put("operation", "CREATE_CERTIFICATION_TEMPLATE");
        try {
            log.info("Creating certification template with title: {}", dto.getTitle());
            validateCertificationTemplate(dto);
            
            CertificationTemplate entity = certificationTemplateMapper.toEntity(dto);
            CertificationTemplate saved = certificationTemplateRepository.save(entity);
            
            log.info("Successfully created certification template with id: {} and title: {}", saved.getId(), saved.getTitle());
            return certificationTemplateMapper.toDTO(saved);
        } catch (ValidationException e) {
            log.warn("Validation error while creating certification template: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating certification template", e);
            throw e;
        } finally {
            MDC.remove("operation");
        }
    }
    
    @Transactional
    public CertificationTemplateDTO update(Long id, CertificationTemplateDTO dto) {
        MDC.put("operation", "UPDATE_CERTIFICATION_TEMPLATE");
        try {
            log.info("Updating certification template with id: {}", id);
            validateCertificationTemplate(dto);
            
            CertificationTemplate entity = certificationTemplateRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Certification template not found with id: {}", id);
                        return new ResourceNotFoundException("Certification template not found with id: " + id);
                    });
            
            certificationTemplateMapper.updateEntityFromDTO(dto, entity);
            CertificationTemplate updated = certificationTemplateRepository.save(entity);
            
            log.info("Successfully updated certification template with id: {}", id);
            return certificationTemplateMapper.toDTO(updated);
        } catch (ValidationException e) {
            log.warn("Validation error while updating certification template with id {}: {}", id, e.getMessage());
            throw e;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating certification template with id: {}", id, e);
            throw e;
        } finally {
            MDC.remove("operation");
        }
    }
    
    @Transactional(readOnly = true)
    public List<CertificationTemplateDTO> findAll() {
        MDC.put("operation", "FIND_ALL_CERTIFICATION_TEMPLATES");
        try {
            log.info("Retrieving all certification templates");
            List<CertificationTemplateDTO> templates = certificationTemplateRepository.findAll().stream()
                    .map(certificationTemplateMapper::toDTO)
                    .collect(Collectors.toList());
            log.info("Successfully retrieved {} certification templates", templates.size());
            return templates;
        } catch (Exception e) {
            log.error("Error retrieving all certification templates", e);
            throw e;
        } finally {
            MDC.remove("operation");
        }
    }
    
    @Transactional(readOnly = true)
    public Page<CertificationTemplateDTO> findAllPaginated(Pageable pageable) {
        MDC.put("operation", "FIND_ALL_CERTIFICATION_TEMPLATES_PAGINATED");
        try {
            log.info("Retrieving certification templates with pagination - page: {}, size: {}", 
                    pageable.getPageNumber(), pageable.getPageSize());
            Page<CertificationTemplate> page = certificationTemplateRepository.findAll(pageable);
            Page<CertificationTemplateDTO> dtoPage = page.map(certificationTemplateMapper::toDTO);
            log.info("Successfully retrieved page {} with {} certification templates", 
                    pageable.getPageNumber(), dtoPage.getNumberOfElements());
            return dtoPage;
        } catch (Exception e) {
            log.error("Error retrieving paginated certification templates", e);
            throw e;
        } finally {
            MDC.remove("operation");
        }
    }
    
    @Transactional(readOnly = true)
    public CertificationTemplateDTO findById(Long id) {
        MDC.put("operation", "FIND_CERTIFICATION_TEMPLATE_BY_ID");
        try {
            log.info("Retrieving certification template with id: {}", id);
            CertificationTemplate entity = certificationTemplateRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Certification template not found with id: {}", id);
                        return new ResourceNotFoundException("Certification template not found with id: " + id);
                    });
            
            log.info("Successfully retrieved certification template with id: {}", id);
            return certificationTemplateMapper.toDTO(entity);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving certification template with id: {}", id, e);
            throw e;
        } finally {
            MDC.remove("operation");
        }
    }
    
    @Transactional
    public void delete(Long id) {
        MDC.put("operation", "DELETE_CERTIFICATION_TEMPLATE");
        try {
            log.info("Deleting certification template with id: {}", id);
            CertificationTemplate entity = certificationTemplateRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Certification template not found with id: {}", id);
                        return new ResourceNotFoundException("Certification template not found with id: " + id);
                    });
            
            // Check if there are any earned certifications referencing this template
            if (entity.getEarnedCertifications() != null && !entity.getEarnedCertifications().isEmpty()) {
                int count = entity.getEarnedCertifications().size();
                log.warn("Cannot delete certification template with id: {} - {} earned certification(s) reference this template", id, count);
                throw new ValidationException(
                    String.format("Cannot delete this certification template. %d learner(s) have earned this certification. " +
                                "Deleting it would remove their achievements.", count)
                );
            }
            
            // Delete the template
            certificationTemplateRepository.delete(entity);
            log.info("Successfully deleted certification template with id: {}", id);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting certification template with id: {}", id, e);
            throw new RuntimeException("Failed to delete certification template: " + e.getMessage(), e);
        } finally {
            MDC.remove("operation");
        }
    }
    
    private void validateCertificationTemplate(CertificationTemplateDTO dto) {
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new ValidationException("Certification title is required");
        }
        
        if (dto.getTitle().length() > 200) {
            throw new ValidationException("Certification title must not exceed 200 characters");
        }
        
        if (dto.getDescription() != null && dto.getDescription().length() > 1000) {
            throw new ValidationException("Description must not exceed 1000 characters");
        }
    }
}
