package com.smartek.sponsor.dto;

import com.smartek.sponsor.entity.Contract;
import com.smartek.sponsor.entity.Sponsor;
import com.smartek.sponsor.entity.Sponsorship;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SponsorDashboardDTO {
    private Sponsor sponsor;
    private List<Contract> contracts;
    private List<Sponsorship> sponsorships;
    private Double totalContractAmount;
    private Double totalSpent;
    private Double remainingBalance;
}

