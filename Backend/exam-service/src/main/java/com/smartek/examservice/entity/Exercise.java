package com.smartek.examservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exercises")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exercise {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;
    
    @Column(nullable = false)
    private Integer exerciseNumber; // Numéro de l'exercice (1, 2, 3...)
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // Contenu de l'exercice
    
    @Column(nullable = false)
    private Integer marks; // Points attribués à cet exercice
    
    @Column(columnDefinition = "TEXT")
    private String instructions; // Instructions spécifiques
}
