package com.simul.common.dto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

import java.util.List;

/**
 * 공통 페이지네이션 응답 DTO
 * API마다 응답 포맷이 파편화되는 것을 방지합니다.
 */
public record PageResponse<T>(
    List<T> data,
    int current_page,
    int per_page,
    Long total_elements,
    Integer total_pages,
    boolean has_next
) {
    /**
     * Spring Data JPA의 Page 객체를 공통 DTO로 변환
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber() + 1, // Spring Page는 0부터 시작하므로 클라이언트 편의를 위해 1로 보정
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.hasNext()
        );
    }

    /**
     * Spring Data JPA의 Slice 객체(무한 스크롤 등)를 공통 DTO로 변환
     */
    public static <T> PageResponse<T> of(Slice<T> slice) {
        return new PageResponse<>(
            slice.getContent(),
            slice.getNumber() + 1,
            slice.getSize(),
            null, // Slice는 전체 개수를 조회하지 않으므로 null
            null, // Slice는 전체 페이지 수를 알 수 없으므로 null
            slice.hasNext()
        );
    }
}
