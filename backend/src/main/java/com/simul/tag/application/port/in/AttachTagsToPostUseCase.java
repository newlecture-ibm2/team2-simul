package com.simul.tag.application.port.in;

import java.util.List;
import java.util.UUID;

public interface AttachTagsToPostUseCase {
    void attachTags(UUID postId, List<String> tagNames);
}
