package com.schoolmanagement.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlobalAnnouncementDTO {

    private Long id;
    private String title;
    private String message;
    private String targetAudience;
    private String sentBy;
    private LocalDateTime sentAt;
}
