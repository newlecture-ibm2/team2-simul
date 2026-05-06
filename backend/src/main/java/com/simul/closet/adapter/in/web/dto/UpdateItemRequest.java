package com.simul.closet.adapter.in.web.dto;

import com.simul.closet.domain.model.Category;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateItemRequest {
    private Category category;
    private String memo;
}
