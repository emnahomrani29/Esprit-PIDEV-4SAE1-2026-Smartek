package com.smartek.examservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_drafts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamDraft {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long examId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long questionId;

    @Column(columnDefinition = "TEXT")
    private String answer;

    @Column(nullable = false)
    private LocalDateTime savedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.savedAt = LocalDateTime.now();
    }
}
