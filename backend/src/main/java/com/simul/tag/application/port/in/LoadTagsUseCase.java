package com.simul.tag.application.port.in;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface LoadTagsUseCase {
    Map<UUID, List<String>> loadTagsByPostIds(List<UUID> postIds);
}
