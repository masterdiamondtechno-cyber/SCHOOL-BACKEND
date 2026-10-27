package com.schoolmanagement.parent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentTeacherMessageDTO {
    private Long id;
    private Long schoolId;
    private Long studentId;
    private String studentName;
    private String standard;
    private String division;
    private Long teacherId;
    private String teacherName;
    private String senderType; // PARENT or TEACHER
    private String senderName;
    private String messageText;
    private String sentAt;
    private Boolean isRead;
}
