package com.schoolmanagement.school.dto;

import com.schoolmanagement.school.entity.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketStatusRequestDTO {
    private Long ticketId;
    private TicketStatus status;
}
