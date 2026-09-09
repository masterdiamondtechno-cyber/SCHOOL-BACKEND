package com.schoolmanagement.app_admin_DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetAllOwnersRequestDTO {

    private int page = 0;
    private int size = 10;
    private String search;
    private String status;
    private String sortBy = "name";
    private String sortDirection = "asc";
}
