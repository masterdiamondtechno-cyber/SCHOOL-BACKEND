package com.schoolmanagement.school_admin_DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromotionRequestDTO {
    private Long schoolId;
    private Long fromAcademicYearId;
    private Long toAcademicYearId;
    private Long fromStandardId;
    private Long fromDivisionId;
    private Long toStandardId;
    private Long toDivisionId;
    private List<StudentPromotionEntryDTO> students;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StudentPromotionEntryDTO {
        private Long studentId;
        private String newRollNo;
        private String promotionStatus; // PROMOTED, DETAINED
    }
}
