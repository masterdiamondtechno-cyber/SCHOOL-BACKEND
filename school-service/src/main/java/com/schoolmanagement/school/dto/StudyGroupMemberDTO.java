package com.schoolmanagement.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudyGroupMemberDTO {

    private Long id;
    private Long studyGroupId;
    private String studyGroupName;
    private Long studentId;
    private String studentName;
    private String role;
    private Long schoolId;
    private Long academicYearId;
    private LocalDateTime joinedAt;
    private List<Long> studentIds; // for bulk add

}
