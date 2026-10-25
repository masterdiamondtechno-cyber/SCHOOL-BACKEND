package com.schoolmanagement.owner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OwnerTeachersRequestDTO {
    private Long ownerId;
    private Long schoolId;
    private String status;
    private String search;
    private int page = 0;
    private int size = 10;

}
