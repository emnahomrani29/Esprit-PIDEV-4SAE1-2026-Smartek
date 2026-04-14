package com.smartek.event.dto;

import com.smartek.event.model.EventStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventStatusChangeRequest {
    
    @NotNull(message = "New status is required")
    private EventStatus newStatus;
    
    private String reason;
    
    @NotNull(message = "Changed by user ID is required")
    private Long changedBy;
}