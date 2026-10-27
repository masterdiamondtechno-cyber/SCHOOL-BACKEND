package com.schoolmanagement.parent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequestDTO {
    private Long schoolId;
    private Long studentId;
    private Long teacherId;
    private String senderType; // PARENT or TEACHER
    private String senderName;
    private String messageText;
}
