package com.smartek.sponsor.dto;

public class PlatformStatsDTO {
    private Double totalBudget;
    private Long totalSponsors;
    private Long totalSponsorships;
    private Long pendingApprovals;
    private Double budgetGrowth;
    private Double sponsorGrowth;
    private Double sponsorshipGrowth;
    private Double approvalRate;
    private Double avgResponseTime;
    private Long activeContracts;

    // Default constructor
    public PlatformStatsDTO() {}

    // Constructor with all fields
    public PlatformStatsDTO(Double totalBudget, Long totalSponsors, Long totalSponsorships, 
                           Long pendingApprovals, Double budgetGrowth, Double sponsorGrowth, 
                           Double sponsorshipGrowth, Double approvalRate, Double avgResponseTime, 
                           Long activeContracts) {
        this.totalBudget = totalBudget;
        this.totalSponsors = totalSponsors;
        this.totalSponsorships = totalSponsorships;
        this.pendingApprovals = pendingApprovals;
        this.budgetGrowth = budgetGrowth;
        this.sponsorGrowth = sponsorGrowth;
        this.sponsorshipGrowth = sponsorshipGrowth;
        this.approvalRate = approvalRate;
        this.avgResponseTime = avgResponseTime;
        this.activeContracts = activeContracts;
    }

    // Getters and Setters
    public Double getTotalBudget() {
        return totalBudget;
    }

    public void setTotalBudget(Double totalBudget) {
        this.totalBudget = totalBudget;
    }

    public Long getTotalSponsors() {
        return totalSponsors;
    }

    public void setTotalSponsors(Long totalSponsors) {
        this.totalSponsors = totalSponsors;
    }

    public Long getTotalSponsorships() {
        return totalSponsorships;
    }

    public void setTotalSponsorships(Long totalSponsorships) {
        this.totalSponsorships = totalSponsorships;
    }

    public Long getPendingApprovals() {
        return pendingApprovals;
    }

    public void setPendingApprovals(Long pendingApprovals) {
        this.pendingApprovals = pendingApprovals;
    }

    public Double getBudgetGrowth() {
        return budgetGrowth;
    }

    public void setBudgetGrowth(Double budgetGrowth) {
        this.budgetGrowth = budgetGrowth;
    }

    public Double getSponsorGrowth() {
        return sponsorGrowth;
    }

    public void setSponsorGrowth(Double sponsorGrowth) {
        this.sponsorGrowth = sponsorGrowth;
    }

    public Double getSponsorshipGrowth() {
        return sponsorshipGrowth;
    }

    public void setSponsorshipGrowth(Double sponsorshipGrowth) {
        this.sponsorshipGrowth = sponsorshipGrowth;
    }

    public Double getApprovalRate() {
        return approvalRate;
    }

    public void setApprovalRate(Double approvalRate) {
        this.approvalRate = approvalRate;
    }

    public Double getAvgResponseTime() {
        return avgResponseTime;
    }

    public void setAvgResponseTime(Double avgResponseTime) {
        this.avgResponseTime = avgResponseTime;
    }

    public Long getActiveContracts() {
        return activeContracts;
    }

    public void setActiveContracts(Long activeContracts) {
        this.activeContracts = activeContracts;
    }
}