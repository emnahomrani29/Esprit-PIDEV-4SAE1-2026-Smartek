package com.smartek.event.dto;

import com.smartek.event.model.EventMode;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRegistrationRequest {
    
    @NotNull(message = "Event ID is required")
    private Long eventId;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Participation mode is required")
    private EventMode participationMode;
}