package com.schoolmanagement.owner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OwnerSchoolsRequestDTO {
    private Long ownerId;
    private String status;
    private String city;
    private String search;
    private int page = 0;
    private int size = 10;

}
