package com.simul.tryon.adapter.out.vision;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.SafeSearchAnnotation;
import com.google.protobuf.ByteString;
import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.tryon.application.port.out.SafeSearchPort;
import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GoogleVisionSafeSearchAdapter implements SafeSearchPort {

    @Override
    public SafeSearchResult analyze(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이미지 바이트가 비어있습니다.");
        }

        try (ImageAnnotatorClient visionClient = ImageAnnotatorClient.create()) {
            Image image = Image.newBuilder().setContent(ByteString.copyFrom(imageBytes)).build();

            Feature feature = Feature.newBuilder()
                    .setType(Feature.Type.SAFE_SEARCH_DETECTION)
                    .build();

            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feature)
                    .setImage(image)
                    .build();

            BatchAnnotateImagesResponse response = visionClient.batchAnnotateImages(List.of(request));
            AnnotateImageResponse res = response.getResponses(0);

            if (res.hasError()) {
                throw new BusinessException(ErrorCode.VISION_API_FAILED, res.getError().getMessage());
            }

            SafeSearchAnnotation safe = res.getSafeSearchAnnotation();
            return new SafeSearchResult(
                    mapLikelihood(safe.getAdult()),
                    mapLikelihood(safe.getViolence()),
                    mapLikelihood(safe.getRacy())
            );
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.VISION_API_FAILED, "Vision API 호출 중 오류가 발생했습니다.");
        }
    }

    private static Likelihood mapLikelihood(com.google.cloud.vision.v1.Likelihood likelihood) {
        if (likelihood == null) return Likelihood.UNKNOWN;
        return switch (likelihood) {
            case VERY_UNLIKELY -> Likelihood.VERY_UNLIKELY;
            case UNLIKELY -> Likelihood.UNLIKELY;
            case POSSIBLE -> Likelihood.POSSIBLE;
            case LIKELY -> Likelihood.LIKELY;
            case VERY_LIKELY -> Likelihood.VERY_LIKELY;
            default -> Likelihood.UNKNOWN;
        };
    }
}

