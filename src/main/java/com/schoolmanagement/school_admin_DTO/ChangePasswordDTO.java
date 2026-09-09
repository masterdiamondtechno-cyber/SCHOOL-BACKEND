package com.schoolmanagement.school_admin_DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordDTO {

    @NotNull(message = "'studentId' is required")
    private Long studentId;

    @NotNull(message = "'schoolId' is required")
    private Long schoolId;

    @NotBlank(message = "'currentPassword' is required")
    private String currentPassword;

    @NotBlank(message = "'newPassword' is required")
    private String newPassword;

    @NotBlank(message = "'confirmPassword' is required")
    private String confirmPassword;

}
