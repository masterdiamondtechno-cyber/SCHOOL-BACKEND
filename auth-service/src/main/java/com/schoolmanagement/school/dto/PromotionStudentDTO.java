package com.schoolmanagement.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromotionStudentDTO {
    private Long studentId;
    private Long fromClassId;
    private Long toClassId;
    private Long toSectionId;
    private String status; // e.g., PROMOTED, DETAINED

}
