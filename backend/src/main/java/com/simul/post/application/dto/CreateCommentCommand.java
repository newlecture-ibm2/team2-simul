package com.simul.post.application.dto;

import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateCommentCommand {
    private String content;
    private UUID parentCommentId;
}
