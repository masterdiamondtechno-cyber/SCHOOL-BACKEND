package com.schoolmanagement.school.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePasswordDTO {

    @NotNull(message = "'studentId' is required")
    private Long studentId;

    @NotNull(message = "'schoolId' is required")
    private Long schoolId;

    @NotBlank(message = "'oldPassword' is required")
    private String oldPassword;

    @NotBlank(message = "'newPassword' is required")
    private String newPassword;

    @NotBlank(message = "'confirmPassword' is required")
    private String confirmPassword;

}
