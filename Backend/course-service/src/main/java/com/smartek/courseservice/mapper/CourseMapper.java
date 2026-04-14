package com.smartek.courseservice.mapper;

import com.smartek.courseservice.dto.ChapterResponse;
import com.smartek.courseservice.dto.CourseRequest;
import com.smartek.courseservice.dto.CourseResponse;
import com.smartek.courseservice.entity.Chapter;
import com.smartek.courseservice.entity.Course;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CourseMapper {

    public Course toEntity(CourseRequest request) {
        if (request == null) {
            return null;
        }
        
        return Course.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .duration(request.getDuration())
                .trainerId(request.getTrainerId())
                .build();
    }

    public CourseResponse toResponse(Course course) {
        return toResponse(course, null);
    }

    public CourseResponse toResponse(Course course, String message) {
        if (course == null) {
            return null;
        }
        
        List<ChapterResponse> chapters = course.getChapters() != null 
                ? course.getChapters().stream()
                    .map(this::toChapterResponse)
                    .collect(Collectors.toList())
                : List.of();
        
        return CourseResponse.builder()
                .courseId(course.getCourseId())
                .title(course.getTitle())
                .content(course.getContent())
                .duration(course.getDuration())
                .trainerId(course.getTrainerId())
                .chapters(chapters)
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .message(message)
                .build();
    }

    public ChapterResponse toChapterResponse(Chapter chapter) {
        if (chapter == null) {
            return null;
        }
        
        return ChapterResponse.builder()
                .chapterId(chapter.getChapterId())
                .title(chapter.getTitle())
                .description(chapter.getDescription())
                .orderIndex(chapter.getOrderIndex())
                .pdfFileName(chapter.getPdfFileName())
                .pdfFilePath(chapter.getPdfFilePath())
                .courseId(chapter.getCourse() != null ? chapter.getCourse().getCourseId() : null)
                .createdAt(chapter.getCreatedAt())
                .updatedAt(chapter.getUpdatedAt())
                .build();
    }

    public void updateEntityFromRequest(Course course, CourseRequest request) {
        if (course == null || request == null) {
            return;
        }
        
        course.setTitle(request.getTitle());
        course.setContent(request.getContent());
        course.setDuration(request.getDuration());
    }
}
