package com.smartek.sponsor.dto;

public class TopSponsorDTO {
    private Long id;
    private String name;
    private String companyName;
    private Double totalAmount;
    private Long sponsorshipCount;

    // Default constructor
    public TopSponsorDTO() {}

    // Constructor with all fields
    public TopSponsorDTO(Long id, String name, String companyName, Double totalAmount, Long sponsorshipCount) {
        this.id = id;
        this.name = name;
        this.companyName = companyName;
        this.totalAmount = totalAmount;
        this.sponsorshipCount = sponsorshipCount;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getSponsorshipCount() {
        return sponsorshipCount;
    }

    public void setSponsorshipCount(Long sponsorshipCount) {
        this.sponsorshipCount = sponsorshipCount;
    }
}