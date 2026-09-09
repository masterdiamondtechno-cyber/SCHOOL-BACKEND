package com.schoolmanagement.school_admin_DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDTO {

    @NotBlank(message = "Email or Phone is required")
    private String username; // email OR phone

    @NotBlank(message = "Password is required")
    private String password;

    private String role; // SCHOOL_ADMIN, STUDENT, TEACHER, APP_ADMIN

    private String otp;

}
