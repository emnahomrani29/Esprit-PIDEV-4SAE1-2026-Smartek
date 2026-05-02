package com.smartek.sponsor.dto;

import java.time.LocalDate;

public class ExpiringContractDTO {
    private Long id;
    private String contractNumber;
    private LocalDate endDate;
    private String sponsorName;
    private Integer daysUntilExpiry;

    // Default constructor
    public ExpiringContractDTO() {}

    // Constructor with all fields
    public ExpiringContractDTO(Long id, String contractNumber, LocalDate endDate, String sponsorName, Integer daysUntilExpiry) {
        this.id = id;
        this.contractNumber = contractNumber;
        this.endDate = endDate;
        this.sponsorName = sponsorName;
        this.daysUntilExpiry = daysUntilExpiry;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getSponsorName() {
        return sponsorName;
    }

    public void setSponsorName(String sponsorName) {
        this.sponsorName = sponsorName;
    }

    public Integer getDaysUntilExpiry() {
        return daysUntilExpiry;
    }

    public void setDaysUntilExpiry(Integer daysUntilExpiry) {
        this.daysUntilExpiry = daysUntilExpiry;
    }
}