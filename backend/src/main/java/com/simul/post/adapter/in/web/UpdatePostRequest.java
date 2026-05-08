package com.simul.post.adapter.in.web;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdatePostRequest {
    private String caption;
    private Boolean isPublic;
    private List<String> tags;
}
