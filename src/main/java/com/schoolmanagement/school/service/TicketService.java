package com.schoolmanagement.school.service;

import com.schoolmanagement.school.dto.*;
import com.schoolmanagement.school.entity.TicketPriority;
import com.schoolmanagement.school.entity.TicketStatus;
import org.springframework.data.domain.Page;

public interface TicketService {
    void createTicket(TicketRequestDTO dto);
    void updatePriority(Long id, TicketPriority priority);
    void updateStatus(Long id, TicketStatus status);
    void updateStatusAndNotes(Long id, TicketStatus status, String internalNotes);
    void updateNotes(Long id, String internalNotes);
    void reopen(Long id);
    Page<TicketResponseDTO> getTickets(
            Long schoolId,
            String role,
            Long standardId,
            Long divisionId,
            TicketPriority priority,
            TicketStatus status,
            int page,
            int limit
    );
}
