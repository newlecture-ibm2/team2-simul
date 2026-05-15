package com.simul.tag.application.dto;

import java.util.List;
import java.util.Map;

public record PostTagsResponse(
        List<String> tags,
        Map<String, List<String>> imageTagsMap,
        List<String> manualTags
) {
}
