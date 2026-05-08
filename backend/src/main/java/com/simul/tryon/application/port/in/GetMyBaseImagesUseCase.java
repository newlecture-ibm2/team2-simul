package com.simul.tryon.application.port.in;

import com.simul.tryon.application.dto.MyBaseImagesResponse;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

public interface GetMyBaseImagesUseCase {
    MyBaseImagesResponse getMyBaseImages(GetMyBaseImagesQuery query);

    @Getter
    @Builder
    class GetMyBaseImagesQuery {
        private final UUID userId;
    }
}

