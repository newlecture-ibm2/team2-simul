package com.simul.closet.adapter.out.persistence;

import com.simul.closet.domain.model.ClosetCollection;
import lombok.Getter;

@Getter
public class CollectionWithCountDto {
    private final ClosetCollection collection;
    private final long itemCount;

    public CollectionWithCountDto(ClosetCollection collection, long itemCount) {
        this.collection = collection;
        this.itemCount = itemCount;
    }
}
