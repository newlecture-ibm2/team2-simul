package com.simul.tryon.adapter.out.vision;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.LocalizedObjectAnnotation;
import com.google.cloud.vision.v1.NormalizedVertex;
import com.google.protobuf.ByteString;
import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.tryon.application.port.out.TryonResultQualityPort;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GoogleVisionTryonResultQualityAdapter implements TryonResultQualityPort {

    private static final float EDGE_MARGIN = 0.03f;
    private static final float CENTER_OFFSET_THRESHOLD = 0.15f;

    @Override
    public TryonResultQualityResult validate(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            return new TryonResultQualityResult(false, "EMPTY_RESULT_IMAGE");
        }

        try (ImageAnnotatorClient visionClient = ImageAnnotatorClient.create()) {
            Image image = Image.newBuilder().setContent(ByteString.copyFrom(imageBytes)).build();
            Feature objectFeature = Feature.newBuilder()
                    .setType(Feature.Type.OBJECT_LOCALIZATION)
                    .build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .setImage(image)
                    .addFeatures(objectFeature)
                    .build();

            BatchAnnotateImagesResponse response = visionClient.batchAnnotateImages(List.of(request));
            AnnotateImageResponse result = response.getResponses(0);
            if (result.hasError()) {
                throw new BusinessException(ErrorCode.VISION_API_FAILED, result.getError().getMessage());
            }

            LocalizedObjectAnnotation person = result.getLocalizedObjectAnnotationsList().stream()
                    .filter(obj -> "Person".equalsIgnoreCase(obj.getName()))
                    .max(Comparator.comparing(LocalizedObjectAnnotation::getScore))
                    .orElse(null);

            if (person == null) {
                return new TryonResultQualityResult(false, "PERSON_NOT_DETECTED");
            }

            BoundingBox box = BoundingBox.from(person.getBoundingPoly().getNormalizedVerticesList());
            if (box.isTouchingEdge()) {
                return new TryonResultQualityResult(false, "PERSON_CROPPED_AT_EDGE");
            }
            if (!box.isCentered()) {
                return new TryonResultQualityResult(false, "PERSON_NOT_CENTERED");
            }

            return new TryonResultQualityResult(true, "OK");
        } catch (IOException e) {
            log.warn("Try-on quality validation failed due to Vision I/O error", e);
            throw new BusinessException(ErrorCode.VISION_API_FAILED, "Vision API 호출 중 오류가 발생했습니다.");
        }
    }

    private record BoundingBox(float minX, float maxX, float minY, float maxY) {
        private static BoundingBox from(List<NormalizedVertex> vertices) {
            if (vertices == null || vertices.isEmpty()) {
                return new BoundingBox(0f, 1f, 0f, 1f);
            }
            float minX = 1f;
            float maxX = 0f;
            float minY = 1f;
            float maxY = 0f;
            for (NormalizedVertex vertex : vertices) {
                minX = Math.min(minX, vertex.getX());
                maxX = Math.max(maxX, vertex.getX());
                minY = Math.min(minY, vertex.getY());
                maxY = Math.max(maxY, vertex.getY());
            }
            return new BoundingBox(minX, maxX, minY, maxY);
        }

        private boolean isTouchingEdge() {
            return minX <= EDGE_MARGIN
                    || maxX >= 1f - EDGE_MARGIN
                    || minY <= EDGE_MARGIN
                    || maxY >= 1f - EDGE_MARGIN;
        }

        private boolean isCentered() {
            float centerX = (minX + maxX) / 2f;
            float centerY = (minY + maxY) / 2f;
            return Math.abs(centerX - 0.5f) <= CENTER_OFFSET_THRESHOLD
                    && Math.abs(centerY - 0.5f) <= CENTER_OFFSET_THRESHOLD;
        }
    }
}

