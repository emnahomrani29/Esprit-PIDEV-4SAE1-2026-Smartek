package com.smartek.courseservice.dto;

import com.smartek.courseservice.entity.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveSessionResponse {
    private Long sessionId;
    private Long courseId;
    private String courseTitle;
    private String title;
    private String description;
    private Long trainerId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String roomId; // ID unique de la salle de visioconférence
    private SessionStatus status;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String message;
}
