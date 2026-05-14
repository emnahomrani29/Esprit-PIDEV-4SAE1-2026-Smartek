package com.smartek.courseservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterRequest {
    
    @NotBlank(message = "Le titre du chapitre est obligatoire")
    private String title;
    
    private String description;
    
    @NotNull(message = "L'ordre du chapitre est obligatoire")
    private Integer orderIndex;
}
