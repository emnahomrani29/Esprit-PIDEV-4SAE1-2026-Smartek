package com.smartek.event.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_status_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @NotNull
    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus newStatus;

    @Column(nullable = false)
    private Long changedBy;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(updatable = false, nullable = false)
    private LocalDateTime changedAt;

    @PrePersist
    protected void onCreate() {
        changedAt = LocalDateTime.now();
    }

    public EventStatusHistory(Long eventId, EventStatus previousStatus, EventStatus newStatus, Long changedBy, String reason) {
        this.eventId = eventId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.reason = reason;
    }
}