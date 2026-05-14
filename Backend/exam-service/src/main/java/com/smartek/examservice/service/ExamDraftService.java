package com.smartek.examservice.service;

import com.smartek.examservice.dto.ExamDraftDTO;
import com.smartek.examservice.entity.ExamDraft;
import com.smartek.examservice.repository.ExamDraftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExamDraftService {
    private final ExamDraftRepository examDraftRepository;

    @Transactional
    public void saveDraft(ExamDraftDTO draftDTO) {
        // Sauvegarder chaque réponse
        draftDTO.getAnswers().forEach((questionId, answer) -> {
            ExamDraft draft = examDraftRepository
                    .findByExamIdAndUserIdAndQuestionId(
                            draftDTO.getExamId(), 
                            draftDTO.getUserId(), 
                            questionId
                    )
                    .orElse(new ExamDraft());
            
            draft.setExamId(draftDTO.getExamId());
            draft.setUserId(draftDTO.getUserId());
            draft.setQuestionId(questionId);
            draft.setAnswer(answer);
            
            examDraftRepository.save(draft);
        });
    }

    public ExamDraftDTO getDraft(Long examId, Long userId) {
        List<ExamDraft> drafts = examDraftRepository.findByExamIdAndUserId(examId, userId);
        
        ExamDraftDTO draftDTO = new ExamDraftDTO();
        draftDTO.setExamId(examId);
        draftDTO.setUserId(userId);
        
        Map<Long, String> answers = new HashMap<>();
        drafts.forEach(draft -> answers.put(draft.getQuestionId(), draft.getAnswer()));
        draftDTO.setAnswers(answers);
        
        return draftDTO;
    }

    @Transactional
    public void deleteDraft(Long examId, Long userId) {
        examDraftRepository.deleteByExamIdAndUserId(examId, userId);
    }
}
