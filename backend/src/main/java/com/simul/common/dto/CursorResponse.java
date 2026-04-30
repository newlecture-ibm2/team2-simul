package com.simul.common.dto;

import java.util.List;

/**
 * 커서(Cursor) 기반 무한 스크롤 공통 응답 DTO
 * 홈 피드(Post) 등 데이터 변화가 잦은 곳에서 데이터 중복/누락을 방지하기 위해 사용합니다.
 * 
 * @param data 실제 반환되는 데이터 리스트
 * @param next_cursor 다음 페이지 조회를 위한 커서 값 (데이터가 없으면 null)
 * @param has_next 다음 페이지가 존재하는지 여부
 */
public record CursorResponse<T>(
    List<T> data,
    String next_cursor,
    boolean has_next
) {
    /**
     * 커서 기반 응답 객체 생성 정적 팩토리 메서드
     */
    public static <T> CursorResponse<T> of(List<T> data, String nextCursor, boolean hasNext) {
        return new CursorResponse<>(data, nextCursor, hasNext);
    }
}
