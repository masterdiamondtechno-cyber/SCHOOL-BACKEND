package com.schoolmanagement.school.dto;

import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordsBulkDTO {

    @NotEmpty(message = "'studentIds' list cannot be empty")
    private List<Long> studentIds;

    @NotNull(message = "'schoolId' is required")
    private Long schoolId;

}
