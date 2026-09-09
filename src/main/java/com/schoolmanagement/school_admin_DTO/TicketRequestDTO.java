package com.schoolmanagement.school_admin_DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketRequestDTO {
    private String title;
    private String description;
    private Long standardId;
    private Long divisionId;
}
