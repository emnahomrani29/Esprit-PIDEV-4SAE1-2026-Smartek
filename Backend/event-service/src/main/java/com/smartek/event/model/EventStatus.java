package com.smartek.event.model;

/**
 * Statuts possibles d'un événement dans le workflow
 */
public enum EventStatus {
    DRAFT("Brouillon"),
    PUBLISHED("Publié"),
    FULL("Complet"),
    ONGOING("En cours"),
    COMPLETED("Terminé"),
    CANCELLED("Annulé");

    private final String displayName;

    EventStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Vérifie si une transition de statut est autorisée
     */
    public boolean canTransitionTo(EventStatus newStatus) {
        switch (this) {
            case DRAFT:
                return newStatus == PUBLISHED || newStatus == CANCELLED;
            case PUBLISHED:
                return newStatus == FULL || newStatus == ONGOING || newStatus == CANCELLED;
            case FULL:
                return newStatus == ONGOING || newStatus == CANCELLED;
            case ONGOING:
                return newStatus == COMPLETED || newStatus == CANCELLED;
            case COMPLETED:
                return false; // Aucune transition possible depuis COMPLETED
            case CANCELLED:
                return newStatus == DRAFT || newStatus == PUBLISHED; // Possibilité de réactiver
            default:
                return false;
        }
    }
}