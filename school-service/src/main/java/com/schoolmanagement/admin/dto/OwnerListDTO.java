package com.schoolmanagement.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OwnerListDTO {

    private Long ownerId;
    private String name;
    private String email;
    private String phoneNo;
    private Long numberOfSchoolsManaged;
}
