package com.smartek.trainingservice.mapper;

import com.smartek.trainingservice.client.ChapterResponse;
import com.smartek.trainingservice.client.CourseResponse;
import com.smartek.trainingservice.dto.TrainingRequest;
import com.smartek.trainingservice.dto.TrainingResponse;
import com.smartek.trainingservice.entity.Training;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TrainingMapper {

    public Training toEntity(TrainingRequest request) {
        if (request == null) {
            return null;
        }
        
        return Training.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .level(request.getLevel())
                .duration(request.getDuration())
                .courseIds(request.getCourseIds() != null ? request.getCourseIds() : List.of())
                .build();
    }

    public TrainingResponse toResponse(Training training) {
        return toResponse(training, null, List.of());
    }

    public TrainingResponse toResponse(Training training, String message) {
        return toResponse(training, message, List.of());
    }

    public TrainingResponse toResponse(Training training, String message, List<CourseResponse> courseResponses) {
        if (training == null) {
            return null;
        }
        
        List<TrainingResponse.CourseInfo> courses = new ArrayList<>();
        
        if (courseResponses != null && !courseResponses.isEmpty()) {
            courses = courseResponses.stream()
                    .map(this::toCourseInfo)
                    .collect(Collectors.toList());
        }
        
        return TrainingResponse.builder()
                .trainingId(training.getTrainingId())
                .title(training.getTitle())
                .description(training.getDescription())
                .category(training.getCategory())
                .level(training.getLevel())
                .duration(training.getDuration())
                .courseIds(training.getCourseIds())
                .courses(courses)
                .createdAt(training.getCreatedAt())
                .updatedAt(training.getUpdatedAt())
                .message(message)
                .build();
    }

    private TrainingResponse.CourseInfo toCourseInfo(CourseResponse courseResponse) {
        if (courseResponse == null) {
            return null;
        }
        
        List<TrainingResponse.ChapterInfo> chapters = courseResponse.getChapters() != null
                ? courseResponse.getChapters().stream()
                    .map(this::toChapterInfo)
                    .collect(Collectors.toList())
                : List.of();
        
        LocalDate duration = null;
        try {
            if (courseResponse.getDuration() != null) {
                duration = LocalDate.parse(courseResponse.getDuration());
            }
        } catch (Exception e) {
            log.warn("Failed to parse duration: {}", courseResponse.getDuration());
        }
        
        return TrainingResponse.CourseInfo.builder()
                .courseId(courseResponse.getCourseId())
                .title(courseResponse.getTitle())
                .content(courseResponse.getContent())
                .duration(duration)
                .chapters(chapters)
                .build();
    }

    private TrainingResponse.ChapterInfo toChapterInfo(ChapterResponse chapterResponse) {
        if (chapterResponse == null) {
            return null;
        }
        
        return TrainingResponse.ChapterInfo.builder()
                .chapterId(chapterResponse.getChapterId())
                .title(chapterResponse.getTitle())
                .description(chapterResponse.getDescription())
                .orderIndex(chapterResponse.getOrderIndex())
                .pdfFileName(chapterResponse.getPdfFileName())
                .pdfFilePath(chapterResponse.getPdfFilePath())
                .build();
    }

    public void updateEntityFromRequest(Training training, TrainingRequest request) {
        if (training == null || request == null) {
            return;
        }
        
        training.setTitle(request.getTitle());
        training.setDescription(request.getDescription());
        training.setCategory(request.getCategory());
        training.setLevel(request.getLevel());
        training.setDuration(request.getDuration());
        
        if (request.getCourseIds() != null) {
            training.setCourseIds(request.getCourseIds());
        }
    }
}
