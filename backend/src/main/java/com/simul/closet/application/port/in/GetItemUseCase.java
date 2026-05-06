package com.simul.closet.application.port.in;

import com.simul.closet.application.dto.ClosetItemResponse;

import java.util.UUID;

public interface GetItemUseCase {
    ClosetItemResponse getItem(UUID itemId);
}
