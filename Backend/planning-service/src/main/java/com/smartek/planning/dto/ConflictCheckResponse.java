package com.smartek.planning.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConflictCheckResponse {
    private boolean hasConflict;
    private List<ConflictDetail> conflicts;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConflictDetail {
        private String type; // TRAINER, ROOM, TIME_OVERLAP
        private String message;
        private Long conflictingPlanningId;
        private String conflictingPlanningTitle;
    }
}
