package com.smartek.examservice.controller;

import com.smartek.examservice.dto.ExamDraftDTO;
import com.smartek.examservice.service.ExamDraftService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamDraftController {
    private final ExamDraftService examDraftService;

    @PostMapping("/{examId}/draft")
    public ResponseEntity<ExamDraftDTO> saveDraft(
            @PathVariable Long examId,
            @RequestBody ExamDraftDTO draftDTO) {
        draftDTO.setExamId(examId);
        examDraftService.saveDraft(draftDTO);
        return ResponseEntity.ok(draftDTO);
    }

    @GetMapping("/{examId}/draft")
    public ResponseEntity<ExamDraftDTO> getDraft(
            @PathVariable Long examId,
            @RequestParam Long userId) {
        return ResponseEntity.ok(examDraftService.getDraft(examId, userId));
    }

    @DeleteMapping("/{examId}/draft")
    public ResponseEntity<Void> deleteDraft(
            @PathVariable Long examId,
            @RequestParam Long userId) {
        examDraftService.deleteDraft(examId, userId);
        return ResponseEntity.ok().build();
    }
}
    