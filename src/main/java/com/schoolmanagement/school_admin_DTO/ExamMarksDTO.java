package com.schoolmanagement.school_admin_DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExamMarksDTO {

    private Long examMarksId;
    private Long schoolId;
    private Long academicYearId;
    private Long examId;
    private Long studentId;
    private Long subjectId;
    private Double marksObtained;
    private Double maxMarks;
    private String grade;
    private String remark;
    private Boolean isAbsent;

    // For bulk save
    private List<ExamMarksEntryDTO> entries;

    public static class ExamMarksEntryDTO {
        private Long studentId;
        private Double marksObtained;
        private String remark;
        private Boolean isAbsent;

        public Long getStudentId() {
            return studentId;
        }

        public void setStudentId(Long studentId) {
            this.studentId = studentId;
        }

        public Double getMarksObtained() {
            return marksObtained;
        }

        public void setMarksObtained(Double marksObtained) {
            this.marksObtained = marksObtained;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public Boolean getIsAbsent() {
            return isAbsent;
        }

        public void setIsAbsent(Boolean isAbsent) {
            this.isAbsent = isAbsent;
        }
    }

}
