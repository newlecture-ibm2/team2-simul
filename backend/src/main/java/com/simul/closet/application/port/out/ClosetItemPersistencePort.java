package com.simul.closet.application.port.out;

import com.simul.closet.domain.model.ClosetItem;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface ClosetItemPersistencePort {
    ClosetItem save(ClosetItem closetItem);
    Optional<ClosetItem> findById(UUID id);
    List<ClosetItem> findByUserId(UUID userId);
    void delete(ClosetItem closetItem);
}
