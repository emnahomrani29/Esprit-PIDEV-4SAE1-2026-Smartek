package com.smartek.courseservice.entity;

/**
 * Enum représentant le statut d'une session live
 */
public enum SessionStatus {
    /**
     * Session planifiée (à venir)
     */
    SCHEDULED,
    
    /**
     * Session en cours (live)
     */
    ONGOING,
    
    /**
     * Session terminée
     */
    COMPLETED,
    
    /**
     * Session annulée
     */
    CANCELLED
}
