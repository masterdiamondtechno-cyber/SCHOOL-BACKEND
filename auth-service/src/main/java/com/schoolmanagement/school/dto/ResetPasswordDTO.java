package com.schoolmanagement.school.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordDTO {

    @NotNull(message = "'studentId' is required")
    private Long studentId;

    @NotNull(message = "'schoolId' is required")
    private Long schoolId;

}
