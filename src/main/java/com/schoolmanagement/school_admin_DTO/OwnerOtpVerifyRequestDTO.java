package com.schoolmanagement.school_admin_DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OwnerOtpVerifyRequestDTO {

    @NotBlank(message = "Username (email or phone) is required")
    private String username;

    @NotBlank(message = "OTP is required")
    private String otp;

}
