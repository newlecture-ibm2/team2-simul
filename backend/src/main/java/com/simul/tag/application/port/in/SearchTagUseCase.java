package com.simul.tag.application.port.in;

import com.simul.tag.application.dto.TagResponse;
import java.util.List;

public interface SearchTagUseCase {
    List<TagResponse> searchTags(String query);
}
