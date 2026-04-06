package com.smartek.learningmicroservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "learning_paths")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPath {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pathId;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Long learnerId;

    @Column(nullable = false)
    private String learnerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LearningPathStatus status;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false)
    private Integer progress; // Pourcentage 0-100

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = LearningPathStatus.PLANIFIE;
        }
        if (this.progress == null) {
            this.progress = 0;
        }
        if (this.startDate == null) {
            this.startDate = LocalDate.now();
        }
    }
}
