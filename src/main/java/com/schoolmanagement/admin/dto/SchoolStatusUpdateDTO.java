package com.schoolmanagement.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchoolStatusUpdateDTO {

    private Long schoolId;
    private String status; // ACTIVE / INACTIVE
}
