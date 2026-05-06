package com.simul.closet.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClosetItemListResponse {
    private List<ClosetItemResponse> items;
    private boolean hasNext;
    private long totalCount;
}
