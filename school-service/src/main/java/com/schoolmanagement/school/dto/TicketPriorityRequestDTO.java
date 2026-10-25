package com.schoolmanagement.school.dto;

import com.schoolmanagement.school.entity.TicketPriority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketPriorityRequestDTO {
    private Long ticketId;
    private TicketPriority priority;
}
