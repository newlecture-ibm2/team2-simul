package com.simul.post.application.port.in;

import com.simul.post.application.dto.FeedPostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface GetFeedPostsUseCase {
    Page<FeedPostResponse> getFeedPosts(UUID currentUserId, String tab, String sort, Pageable pageable);
}
