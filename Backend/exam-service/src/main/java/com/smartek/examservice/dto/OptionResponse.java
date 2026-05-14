package com.smartek.examservice.dto;

import lombok.Data;

@Data
public class OptionResponse {
    private Long id;
    private String optionText;
    private Boolean isCorrect;
}
