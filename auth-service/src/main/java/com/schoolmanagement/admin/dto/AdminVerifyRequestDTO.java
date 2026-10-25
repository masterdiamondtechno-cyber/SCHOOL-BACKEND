package com.schoolmanagement.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminVerifyRequestDTO {

    @NotBlank(message = "Email or Phone is required")
    private String username;

    @NotBlank(message = "OTP is required")
    private String otp;

}
