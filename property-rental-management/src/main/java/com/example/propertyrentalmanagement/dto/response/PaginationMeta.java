package com.example.propertyrentalmanagement.dto.response;

import org.springframework.data.domain.Page;

public record PaginationMeta(
        int page,
        int pageSize,
        long totalItems,
        int totalPages,
        boolean hastNext
) {
    public static PaginationMeta fromPage(Page<?> page) {
        return new PaginationMeta(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
