package com.simul.closet.adapter.in.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class BulkUpdateItemCollectionRequest {
    private List<UUID> itemIds;
    private UUID collectionId;
}
