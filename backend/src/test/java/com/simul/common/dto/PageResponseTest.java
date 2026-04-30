package com.simul.common.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseTest {

    @Test
    @DisplayName("Spring Page 객체가 PageResponse로 올바르게 변환된다")
    void convertPageToPageResponse() {
        // given
        List<String> content = List.of("A", "B", "C");
        PageRequest pageRequest = PageRequest.of(0, 10); // Spring은 0-indexed (0페이지)
        Page<String> page = new PageImpl<>(content, pageRequest, 15); // 총 15개 아이템 존재 (2페이지까지 있음)

        // when
        PageResponse<String> response = PageResponse.of(page);

        // then
        assertThat(response.data()).containsExactly("A", "B", "C");
        assertThat(response.current_page()).isEqualTo(1); // 클라이언트 응답용으로 +1 보정됨
        assertThat(response.per_page()).isEqualTo(10);
        assertThat(response.total_elements()).isEqualTo(15L);
        assertThat(response.total_pages()).isEqualTo(2); // 15 / 10 = 1.5 -> 2페이지
        assertThat(response.has_next()).isTrue(); // 다음 페이지 존재함
    }

    @Test
    @DisplayName("Spring Slice 객체가 PageResponse로 올바르게 변환된다")
    void convertSliceToPageResponse() {
        // given
        List<String> content = List.of("A", "B", "C");
        PageRequest pageRequest = PageRequest.of(1, 3); // 1페이지 (내부적으로 2번째 페이지)
        Slice<String> slice = new SliceImpl<>(content, pageRequest, true); // 다음 슬라이스 있음

        // when
        PageResponse<String> response = PageResponse.of(slice);

        // then
        assertThat(response.data()).containsExactly("A", "B", "C");
        assertThat(response.current_page()).isEqualTo(2); // 클라이언트 응답용으로 +1 보정됨
        assertThat(response.per_page()).isEqualTo(3);
        assertThat(response.total_elements()).isNull(); // Slice는 전체 개수를 모름
        assertThat(response.total_pages()).isNull(); // Slice는 전체 페이지 수를 모름
        assertThat(response.has_next()).isTrue();
    }
}
