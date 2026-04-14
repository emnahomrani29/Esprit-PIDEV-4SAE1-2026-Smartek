package com.smartek.courseservice.dto;

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
}
