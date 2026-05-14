package com.smartek.event.model;

/**
 * Statuts d'inscription à un événement
 */
public enum RegistrationStatus {
    CONFIRMED("Confirmé"),
    WAITING("En attente"),
    CANCELLED("Annulé");

    private final String displayName;

    RegistrationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}