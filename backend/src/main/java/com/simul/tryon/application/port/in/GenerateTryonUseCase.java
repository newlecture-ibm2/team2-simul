package com.simul.tryon.application.port.in;

import com.simul.tryon.application.dto.TryonGenerateResponse;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

public interface GenerateTryonUseCase {
    TryonGenerateResponse generate(GenerateTryonCommand command);

    @Getter
    @Builder
    class GenerateTryonCommand {
        private final UUID userId;
        private final UUID baseImageId;
        private final UUID itemId;
        private final List<UUID> itemIds;
    }
}
