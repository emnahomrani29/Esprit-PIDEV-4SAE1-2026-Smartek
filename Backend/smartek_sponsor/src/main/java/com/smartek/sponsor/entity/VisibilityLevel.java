package com.smartek.sponsor.entity;

public enum VisibilityLevel {
    LOGO(500.0),
    FEATURED(1500.0),
    TITLE(3000.0);

    private final Double minimumAmount;

    VisibilityLevel(Double minimumAmount) {
        this.minimumAmount = minimumAmount;
    }

    public Double getMinimumAmount() {
        return minimumAmount;
    }

    public static VisibilityLevel suggestTier(Double amount) {
        if (amount >= TITLE.minimumAmount) {
            return TITLE;
        } else if (amount >= FEATURED.minimumAmount) {
            return FEATURED;
        } else if (amount >= LOGO.minimumAmount) {
            return LOGO;
        }
        return null;
    }
}

