package sk.eventfindr.fsa.domain;

import java.util.Collection;

public record PaginatedResult<T>(
        Collection<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static <T> PaginatedResult<T> of(Collection<T> content, int page, int size, long totalElements) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        boolean last = page >= totalPages - 1;
        return new PaginatedResult<>(content, page, size, totalElements, totalPages, last);
    }
}
