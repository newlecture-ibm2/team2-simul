package com.simul.common.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CursorResponseTest {

    @Test
    @DisplayName("커서 기반 응답(CursorResponse) 객체가 올바르게 생성된다")
    void createCursorResponse() {
        // given
        List<String> data = List.of("post1", "post2", "post3");
        String nextCursor = "uuid-last-post3";
        boolean hasNext = true;

        // when
        CursorResponse<String> response = CursorResponse.of(data, nextCursor, hasNext);

        // then
        assertThat(response.data()).containsExactly("post1", "post2", "post3");
        assertThat(response.next_cursor()).isEqualTo(nextCursor);
        assertThat(response.has_next()).isTrue();
    }

    @Test
    @DisplayName("다음 페이지가 없을 경우 CursorResponse 객체가 올바르게 생성된다")
    void createCursorResponseWhenNoNextPage() {
        // given
        List<String> data = List.of("post1", "post2");
        String nextCursor = null; // 다음 페이지가 없으므로 null
        boolean hasNext = false;

        // when
        CursorResponse<String> response = CursorResponse.of(data, nextCursor, hasNext);

        // then
        assertThat(response.data()).containsExactly("post1", "post2");
        assertThat(response.next_cursor()).isNull();
        assertThat(response.has_next()).isFalse();
    }
}
