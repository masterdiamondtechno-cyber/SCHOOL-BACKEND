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
public class ResetTeacherPasswordsBulkDTO {

    @NotEmpty(message = "'teacherIds' list cannot be empty")
    private List<Long> teacherIds;

    @NotNull(message = "'schoolId' is required")
    private Long schoolId;

}
