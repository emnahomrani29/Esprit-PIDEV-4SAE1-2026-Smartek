package com.smartek.certificationbadgeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkAwardResponseDTO {
    
    private Integer successCount;
    
    private Integer failureCount;
    
    private List<AwardResultDTO> results;
    
    public BulkAwardResponseDTO(List<AwardResultDTO> results) {
        this.results = results;
        this.successCount = (int) results.stream().filter(AwardResultDTO::getSuccess).count();
        this.failureCount = results.size() - this.successCount;
    }
}
