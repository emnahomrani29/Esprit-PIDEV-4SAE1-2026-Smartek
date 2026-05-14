package com.smartek.event.model;

/**
 * Modes de participation à un événement
 */
public enum EventMode {
    PHYSICAL("Présentiel"),
    ONLINE("En ligne"),
    HYBRID("Hybride");

    private final String displayName;

    EventMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}