package com.schoolmanagement.school_admin_DTO;

import com.schoolmanagement.school_admin_Entity.TicketStatus;
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
