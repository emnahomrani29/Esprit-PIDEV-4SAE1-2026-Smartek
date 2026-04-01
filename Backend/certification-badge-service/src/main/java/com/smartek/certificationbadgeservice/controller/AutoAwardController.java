package com.smartek.certificationbadgeservice.controller;

import com.smartek.certificationbadgeservice.dto.AutoAwardRequestDTO;
import com.smartek.certificationbadgeservice.dto.EarnedCertificationDTO;
import com.smartek.certificationbadgeservice.exception.ErrorResponse;
import com.smartek.certificationbadgeservice.security.InternalApiAuthService;
import com.smartek.certificationbadgeservice.service.EarnedCertificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/certifications")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
@Slf4j
public class AutoAwardController {
    
    private final EarnedCertificationService earnedCertificationService;
    private final InternalApiAuthService internalApiAuthService;
    
    @PostMapping("/auto-award")
    public ResponseEntity<?> autoAwardCertification(
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String apiKey,
            @Valid @RequestBody AutoAwardRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        if (!internalApiAuthService.validate(apiKey)) {
            ErrorResponse err = new ErrorResponse(
                    java.time.LocalDateTime.now(),
                    HttpStatus.UNAUTHORIZED.value(),
                    HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                    "Unauthorized: invalid internal API key",
                    List.of("Provide valid X-Internal-Api-Key header"),
                    httpRequest.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }
        
        log.info("Auto-award request from Exam Service: examId={}, learnerId={}, templateId={}, score={}, completionDate={}",
                request.getExamId(), request.getLearnerId(), request.getCertificationTemplateId(), request.getScore(), request.getCompletionDate());
        
        LocalDate issueDate = request.getCompletionDate().toLocalDate();
        EarnedCertificationDTO dto = earnedCertificationService.autoAwardCertification(
                request.getCertificationTemplateId(),
                request.getLearnerId(),
                issueDate,
                request.getExamId()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
}
