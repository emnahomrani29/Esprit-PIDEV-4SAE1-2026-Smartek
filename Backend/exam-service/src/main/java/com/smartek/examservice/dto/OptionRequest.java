package com.smartek.examservice.dto;

import lombok.Data;

@Data
public class OptionRequest {
    private String optionText;
    private Boolean isCorrect;
}
