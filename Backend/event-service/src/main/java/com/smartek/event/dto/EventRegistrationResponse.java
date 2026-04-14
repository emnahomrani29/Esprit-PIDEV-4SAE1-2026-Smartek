package com.smartek.event.dto;

import com.smartek.event.model.EventMode;
import com.smartek.event.model.PaymentStatus;
import com.smartek.event.model.RegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRegistrationResponse {
    
    private Long registrationId;
    private Long eventId;
    private Long userId;
    private RegistrationStatus status;
    private PaymentStatus paymentStatus;
    private EventMode participationMode;
    private LocalDateTime registeredAt;
    private Integer waitingListPosition;
    private String message; // Message informatif pour l'utilisateur
}