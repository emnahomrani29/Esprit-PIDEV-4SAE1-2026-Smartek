package com.smartek.learningmicroservice.service;

import com.smartek.learningmicroservice.dto.LearningPathRequest;
import com.smartek.learningmicroservice.dto.LearningPathResponse;
import com.smartek.learningmicroservice.entity.LearningPath;
import com.smartek.learningmicroservice.entity.LearningPathStatus;
import com.smartek.learningmicroservice.repository.LearningPathRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearningPathService {

    private final LearningPathRepository pathRepository;

    @Transactional
    public LearningPathResponse createPath(LearningPathRequest request) {
        // Vérifier les doublons
        if (pathRepository.existsByLearnerIdAndTitle(request.getLearnerId(), request.getTitle())) {
            throw new RuntimeException("Un parcours avec ce titre existe déjà pour cet apprenant");
        }

        LearningPath path = LearningPath.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .learnerId(request.getLearnerId())
                .learnerName(request.getLearnerName())
                .status(request.getStatus())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .progress(request.getProgress())
                .build();

        LearningPath savedPath = pathRepository.save(path);
        return mapToResponse(savedPath);
    }

    public List<LearningPathResponse> getAllPathsByLearner(Long learnerId) {
        return pathRepository.findByLearnerIdOrderByStartDateDesc(learnerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<LearningPathResponse> getAllPaths() {
        return pathRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public LearningPathResponse getPathById(Long pathId) {
        LearningPath path = pathRepository.findById(pathId)
                .orElseThrow(() -> new RuntimeException("Parcours d'apprentissage non trouvé"));
        return mapToResponse(path);
    }

    public List<LearningPathResponse> getPathsByStatus(LearningPathStatus status) {
        return pathRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<LearningPathResponse> getPathsByLearnerAndStatus(Long learnerId, LearningPathStatus status) {
        return pathRepository.findByLearnerIdAndStatus(learnerId, status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public LearningPathResponse updatePath(Long pathId, LearningPathRequest request) {
        LearningPath path = pathRepository.findById(pathId)
                .orElseThrow(() -> new RuntimeException("Parcours d'apprentissage non trouvé"));

        // Vérifier les doublons (exclure l'ID actuel)
        if (pathRepository.existsByLearnerIdAndTitleAndPathIdNot(
                path.getLearnerId(), request.getTitle(), pathId)) {
            throw new RuntimeException("Un parcours avec ce titre existe déjà pour cet apprenant");
        }

        path.setTitle(request.getTitle());
        path.setDescription(request.getDescription());
        path.setStatus(request.getStatus());
        path.setStartDate(request.getStartDate());
        path.setEndDate(request.getEndDate());
        path.setProgress(request.getProgress());

        LearningPath updatedPath = pathRepository.save(path);
        return mapToResponse(updatedPath);
    }

    @Transactional
    public void deletePath(Long pathId) {
        if (!pathRepository.existsById(pathId)) {
            throw new RuntimeException("Parcours d'apprentissage non trouvé");
        }
        pathRepository.deleteById(pathId);
    }

    private LearningPathResponse mapToResponse(LearningPath path) {
        return new LearningPathResponse(
                path.getPathId(),
                path.getTitle(),
                path.getDescription(),
                path.getLearnerId(),
                path.getLearnerName(),
                path.getStatus(),
                path.getStartDate(),
                path.getEndDate(),
                path.getProgress()
        );
    }
}
