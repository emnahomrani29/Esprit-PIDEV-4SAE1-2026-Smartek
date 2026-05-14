package com.smartek.planning.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "plannings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Planning {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long planningId;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(nullable = false)
    private LocalTime startTime;
    
    @Column(nullable = false)
    private LocalTime endTime;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false)
    private String eventType; // COURSE, TRAINING, EXAM, MEETING, OTHER
    
    private String location;
    
    @Column(nullable = false)
    private String color; // Couleur pour l'affichage dans le calendrier
    
    @Column(name = "created_by")
    private Long createdBy; // ID de l'utilisateur qui a créé le planning
    
    @Column(name = "trainer_id")
    private Long trainerId; // ID du formateur assigné
    
    @Column(name = "room_id")
    private String roomId; // ID de la salle
    
    @Column(name = "max_participants")
    private Integer maxParticipants; // Capacité maximale
    
    @Column(name = "current_participants")
    private Integer currentParticipants = 0; // Nombre actuel de participants
    
    @Column(name = "status")
    private String status = "SCHEDULED"; // SCHEDULED, COMPLETED, CANCELLED
}
