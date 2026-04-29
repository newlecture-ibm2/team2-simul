package com.simul.closet.adapter.out.persistence;

import com.simul.closet.application.port.out.ClosetItemPersistencePort;
import com.simul.closet.domain.model.ClosetItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ClosetItemPersistenceAdapter implements ClosetItemPersistencePort {

    private final ClosetItemJpaRepository closetItemJpaRepository;

    @Override
    public ClosetItem save(ClosetItem closetItem) {
        return closetItemJpaRepository.save(closetItem);
    }

    @Override
    public Optional<ClosetItem> findById(UUID id) {
        return closetItemJpaRepository.findByIdAndDeletedAtIsNull(id);
    }

    @Override
    public List<ClosetItem> findByUserId(UUID userId) {
        return closetItemJpaRepository.findAllByUserIdAndDeletedAtIsNull(userId);
    }

    @Override
    public void delete(ClosetItem closetItem) {
        closetItem.softDelete();
        closetItemJpaRepository.save(closetItem);
    }
}
