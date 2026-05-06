package com.simul.common.utils;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ImageUtils {

    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            "image/jpeg", "image/png", "image/heic", "image/webp"
    );
    private static final int MAX_WIDTH = 1080;

    /**
     * 이미지 파일 포맷을 검증합니다.
     */
    public static void validateFormat(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED, "파일이 비어있습니다.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_EXTENSIONS.contains(contentType.toLowerCase())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "지원하지 않는 이미지 포맷입니다. (JPG, PNG, HEIC, WebP만 허용)");
        }
    }

    /**
     * 이미지 용량을 검증합니다.
     */
    public static void validateSize(MultipartFile file, long maxSizeInBytes, ErrorCode errorCode) {
        if (file.getSize() > maxSizeInBytes) {
            throw new BusinessException(errorCode, "이미지 용량이 초과되었습니다.");
        }
    }

    /**
     * 해상도를 검증합니다.
     */
    public static void validateResolution(MultipartFile file, int minWidth, int minHeight) {
        try (InputStream is = file.getInputStream()) {
            BufferedImage image = ImageIO.read(is);
            if (image == null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "이미지를 읽을 수 없습니다.");
            }
            if (image.getWidth() < minWidth || image.getHeight() < minHeight) {
                throw new BusinessException(ErrorCode.INAPPROPRIATE_IMAGE, 
                        String.format("해상도가 너무 작습니다. (최소 %dx%d)", minWidth, minHeight));
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED, "이미지 해상도 검증 중 오류가 발생했습니다.");
        }
    }

    /**
     * 이미지가 클 경우, 비율을 유지하며 최대 너비(MAX_WIDTH)에 맞춰 리사이징합니다.
     */
    public static byte[] resizeImageIfLarge(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            BufferedImage image = ImageIO.read(is);
            if (image == null) {
                return file.getBytes(); // 원본 반환
            }

            if (image.getWidth() > MAX_WIDTH) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                Thumbnails.of(image)
                        .width(MAX_WIDTH)
                        .keepAspectRatio(true)
                        .outputFormat("jpg")
                        .toOutputStream(outputStream);
                return outputStream.toByteArray();
            }
            return file.getBytes();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED, "이미지 리사이징 중 오류가 발생했습니다.");
        }
    }
}
