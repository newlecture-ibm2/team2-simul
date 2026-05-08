package com.simul.tryon.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.tryon.application.port.in.GetMyBaseImagesUseCase;
import com.simul.tryon.application.port.out.BaseImagePersistencePort;
import com.simul.tryon.domain.model.BaseImage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GetMyBaseImagesServiceTest {

    private final BaseImagePersistencePort baseImagePersistencePort = mock(BaseImagePersistencePort.class);
    private final GetMyBaseImagesService service = new GetMyBaseImagesService(baseImagePersistencePort);

    @Test
    void getMyBaseImages_requiresUserId() {
        var query = GetMyBaseImagesUseCase.GetMyBaseImagesQuery.builder().userId(null).build();

        assertThatThrownBy(() -> service.getMyBaseImages(query))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    void getMyBaseImages_mapsToResponse() {
        UUID userId = UUID.randomUUID();
        UUID baseImageId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);

        BaseImage baseImage = mock(BaseImage.class);
        when(baseImage.getId()).thenReturn(baseImageId);
        when(baseImage.getUserId()).thenReturn(userId);
        when(baseImage.getImageUrl()).thenReturn("/uploads/images/tryon/x.png");
        when(baseImage.getCreatedAt()).thenReturn(createdAt);

        when(baseImagePersistencePort.findAllActiveByUserId(userId)).thenReturn(List.of(baseImage));

        var query = GetMyBaseImagesUseCase.GetMyBaseImagesQuery.builder().userId(userId).build();
        var response = service.getMyBaseImages(query);

        assertThat(response.baseImages()).hasSize(1);
        assertThat(response.baseImages().getFirst().baseImageId()).isEqualTo(baseImageId);
        assertThat(response.baseImages().getFirst().imageUrl()).isEqualTo("/uploads/images/tryon/x.png");
        assertThat(response.baseImages().getFirst().createdAt()).isEqualTo(createdAt);
    }
}

