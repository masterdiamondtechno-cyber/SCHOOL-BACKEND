package com.schoolmanagement.owner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OwnerStudentsRequestDTO {
    private Long ownerId;
    private Long schoolId;
    private Long classId;
    private String feeStatus;
    private String gender;
    private String search;
    private int page = 0;
    private int size = 10;
}
