package com.simul.post.application.port.in;

import com.simul.post.application.dto.UpdatePostCommand;
import java.util.UUID;

public interface UpdatePostUseCase {
    void updatePost(UpdatePostCommand command);
}
