package com.simul.tryon.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.simul.common.application.port.out.ImageStoragePort;
import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.tryon.application.port.in.UploadBaseImageUseCase;
import com.simul.tryon.application.port.out.BaseImagePersistencePort;
import com.simul.tryon.domain.model.BaseImage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class UploadBaseImageServiceTest {

    private final ImageStoragePort imageStoragePort = mock(ImageStoragePort.class);
    private final BaseImagePersistencePort baseImagePersistencePort = mock(BaseImagePersistencePort.class);
    private final UploadBaseImageService service = new UploadBaseImageService(imageStoragePort, baseImagePersistencePort);

    @Test
    void upload_rejectsTooSmallResolution() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "tiny.png",
                "image/png",
                pngBytes(10, 10)
        );

        UploadBaseImageUseCase.UploadBaseImageCommand command =
                UploadBaseImageUseCase.UploadBaseImageCommand.builder()
                        .userId(UUID.randomUUID())
                        .image(file)
                        .build();

        assertThatThrownBy(() -> service.upload(command))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INAPPROPRIATE_IMAGE);
    }

    @Test
    void upload_acceptsValidPngAndReturnsIds() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID baseImageId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "ok.png",
                "image/png",
                pngBytes(256, 256)
        );

        when(imageStoragePort.uploadImage(eq(file), eq("tryon"))).thenReturn("/uploads/images/tryon/2026/05/08/x.png");

        BaseImage persisted = mock(BaseImage.class);
        when(persisted.getId()).thenReturn(baseImageId);
        when(persisted.getImageUrl()).thenReturn("/uploads/images/tryon/2026/05/08/x.png");
        when(baseImagePersistencePort.save(any(BaseImage.class))).thenReturn(persisted);

        UploadBaseImageUseCase.UploadBaseImageCommand command =
                UploadBaseImageUseCase.UploadBaseImageCommand.builder()
                        .userId(userId)
                        .image(file)
                        .build();

        var response = service.upload(command);
        assertThat(response.baseImageId()).isEqualTo(baseImageId);
        assertThat(response.imageUrl()).isEqualTo("/uploads/images/tryon/2026/05/08/x.png");
    }

    @Test
    void upload_rejectsWebpForBaseImage() {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "no.webp",
                "image/webp",
                new byte[] {1, 2, 3}
        );

        UploadBaseImageUseCase.UploadBaseImageCommand command =
                UploadBaseImageUseCase.UploadBaseImageCommand.builder()
                        .userId(UUID.randomUUID())
                        .image(file)
                        .build();

        assertThatThrownBy(() -> service.upload(command))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }

    private static byte[] pngBytes(int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }
}
