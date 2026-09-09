package com.schoolmanagement.school_admin_DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceDTO {

    private Long id;
    private String title;
    private String resourceType;
    private String fileUrl;
    private Long subjectId;
    private String subjectName;
    private Long standardId;
    private String standardName;
    private Long schoolId;
    private Long academicYearId;
    private String uploadedBy;
    private LocalDateTime createdAt;

}
