package com.smartek.event.model;

/**
 * Statuts de paiement d'une inscription
 */
public enum PaymentStatus {
    PENDING("En attente"),
    PAID("Payé"),
    REFUNDED("Remboursé"),
    FAILED("Échec");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}