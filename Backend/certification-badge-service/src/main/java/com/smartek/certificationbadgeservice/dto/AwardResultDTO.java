package com.smartek.certificationbadgeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AwardResultDTO {
    
    private Long learnerId;
    
    private Boolean success;
    
    private String errorMessage;
    
    public static AwardResultDTO success(Long learnerId) {
        return new AwardResultDTO(learnerId, true, null);
    }
    
    public static AwardResultDTO failure(Long learnerId, String errorMessage) {
        return new AwardResultDTO(learnerId, false, errorMessage);
    }
}
