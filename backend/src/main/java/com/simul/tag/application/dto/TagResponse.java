package com.simul.tag.application.dto;

import java.util.UUID;

public record TagResponse(
    UUID tagId,
    String name,
    String category,
    Integer usageCount
) {}
