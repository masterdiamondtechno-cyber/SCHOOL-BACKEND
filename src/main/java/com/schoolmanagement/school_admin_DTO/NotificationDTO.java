package com.schoolmanagement.school_admin_DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {

    private Long notificationId;
    private Long schoolId;
    private String schoolName;
    private Long academicYearId;
    private String title;
    private String message;
    private String type;
    private String targetAudience;
    private Long targetStandardId;
    private Long targetDivisionId;
    private String sentBy;
    private LocalDate expiryDate;
    private LocalDateTime sentAt;
    private Boolean isActive;

}
