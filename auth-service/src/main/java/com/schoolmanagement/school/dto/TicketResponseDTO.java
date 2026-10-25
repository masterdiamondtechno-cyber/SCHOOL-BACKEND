package com.schoolmanagement.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketResponseDTO {
    private Long id;
    private String ticketId;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String raisedByRole;
    private String school;
    private String standard;
    private String division;
    private LocalDateTime createdAt;
    private String internalNotes;
}
