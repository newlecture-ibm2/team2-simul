package com.simul.tag.adapter.out.vision;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import com.simul.tag.application.port.out.VisionApiPort;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * [Hexagonal - Outbound Adapter]
 * Google Cloud Vision API와 직접 통신하며, 정책에 따라 필터링합니다.
 * - confidence >= 0.7 이상만 통과
 * - 패션과 무관한 노이즈 단어(블랙리스트)만 제거하여 다양한 태그 보장
 */
@Component
public class GoogleVisionTagAdapter implements VisionApiPort {

    // 블랙리스트: 사진에 흔히 나오지만 패션과 무관한 단어만 제거
    private static final Set<String> EXCLUDED_KEYWORDS = Set.of(
            "Person", "People", "Human", "Face", "Smile", "Skin", "Head",
            "Selfie", "Smartphone", "Mobile phone", "Phone",
            "Wall", "Floor", "Sky", "Room", "Building", "Background",
            "Furniture", "Photography", "Hand", "Arm", "Leg",
            "Hair", "Eyebrow", "Lip", "Nose", "Chin", "Forehead",
            "Standing", "Sitting", "Gesture", "Happy", "Cool",
            "Rectangle", "Font", "Circle", "Material property"
    );

    @Override
    public List<String> analyzeImage(MultipartFile file) {
        List<String> recommendedTags = new ArrayList<>();

        try (ImageAnnotatorClient visionClient = ImageAnnotatorClient.create()) {

            ByteString imgBytes = ByteString.readFrom(file.getInputStream());
            Image image = Image.newBuilder().setContent(imgBytes).build();

            Feature feature = Feature.newBuilder()
                    .setType(Feature.Type.LABEL_DETECTION)
                    .setMaxResults(20)  // 최대 20개 후보 요청
                    .build();

            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feature)
                    .setImage(image)
                    .build();

            BatchAnnotateImagesResponse response = visionClient.batchAnnotateImages(List.of(request));
            AnnotateImageResponse res = response.getResponses(0);

            if (res.hasError()) {
                throw new RuntimeException("ERR-307-B: Vision API 오류 - " + res.getError().getMessage());
            }

            for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
                if (annotation.getScore() >= 0.7f) {
                    String label = annotation.getDescription();
                    if (isValidFashionTag(label)) {
                        recommendedTags.add(label);
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("ERR-307-B: 이미지 분석 중 오류 발생", e);
        }

        return recommendedTags;
    }

    private boolean isValidFashionTag(String label) {
        String lowerLabel = label.toLowerCase();
        for (String excluded : EXCLUDED_KEYWORDS) {
            if (lowerLabel.contains(excluded.toLowerCase())) {
                return false;
            }
        }
        return true;
    }
}
