package com.smartek.courseservice.dto;

import com.smartek.courseservice.entity.DeliveryMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseRequest {
    
    @NotBlank(message = "Le titre est obligatoire")
    private String title;
    
    private String content;
    
    @NotNull(message = "La durée est obligatoire")
    private LocalDate duration;
    
    @NotNull(message = "Le trainer ID est obligatoire")
    private Long trainerId;
    
    @NotNull(message = "Le mode de livraison est obligatoire")
    @Builder.Default
    private DeliveryMode deliveryMode = DeliveryMode.PRESENTIEL;
}
