package com.simul.post.application.port.in;

import com.simul.post.application.dto.FeedPostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SearchPostUseCase {
    Page<FeedPostResponse> searchPosts(String query, String type, UUID currentUserId, Pageable pageable);
}
