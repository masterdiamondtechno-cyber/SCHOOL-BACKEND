package com.schoolmanagement.school.dto;

import com.schoolmanagement.school.entity.TicketPriority;
import com.schoolmanagement.school.entity.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketFilterRequestDTO {

    private Long schoolId;
    private String role;
    private Long standardId;
    private Long divisionId;
    private TicketPriority priority;
    private TicketStatus status;
    private int page = 1;
    private int limit = 20;

}
