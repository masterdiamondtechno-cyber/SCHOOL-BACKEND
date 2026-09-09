package com.schoolmanagement.school_admin_DTO;

import com.schoolmanagement.school_admin_Entity.TicketPriority;
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
