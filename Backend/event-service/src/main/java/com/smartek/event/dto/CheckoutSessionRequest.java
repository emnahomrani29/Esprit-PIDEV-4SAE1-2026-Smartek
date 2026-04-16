package com.smartek.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutSessionRequest {
    private Long registrationId;
    private Long eventId;
    private Long userId;
    private Double amount;       // Montant en TND
    private String currency;     // "tnd" ou "usd"
    private String eventTitle;
}
