package com.smartek.sponsor.dto;

import com.smartek.sponsor.entity.SponsorStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SponsorDTO {
    private Long id;
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phone;
    
    @Size(max = 100, message = "Company name cannot exceed 100 characters")
    private String companyName;
    
    @Size(max = 50, message = "Industry cannot exceed 50 characters")
    private String industry;
    
    @Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$", 
             message = "Invalid website URL")
    private String website;
    
    private String logoUrl;
    private SponsorStatus status;
    
    // For registration only
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
