package com.smartek.offersservice.dto;

import lombok.Data;

@Data
public class OfferSearchRequest {
    private String keyword;
    private String contractType;
    private String location;
    private String domain;
    private String experienceLevel;
    private Boolean remote;
    private Integer salaryMin;
    private Integer salaryMax;
    private String sortBy = "createdAt";   // createdAt, viewCount, title
    private String sortDir = "desc";        // asc, desc
    private int page = 0;
    private int size = 10;
}
