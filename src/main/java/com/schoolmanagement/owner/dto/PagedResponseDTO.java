package com.schoolmanagement.owner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PagedResponseDTO<T> {

    private List<T> data;
    private int page;
    private int size;

    public PagedResponseDTO() {
    }

    public PagedResponseDTO(List<T> data, int page, int size, long totalElements) {
        this.data = data;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
    }

    private long totalElements;
    private int totalPages;


}
