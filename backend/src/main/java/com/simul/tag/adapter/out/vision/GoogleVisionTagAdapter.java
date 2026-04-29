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
    // 실제 테스트 결과를 반영하여 지속적으로 보완 중
    private static final Set<String> EXCLUDED_KEYWORDS = Set.of(
            // 인체 부위
            "Person", "People", "Human", "Face", "Smile", "Skin", "Head",
            "Hand", "Arm", "Leg", "Waist", "Thigh", "Neck", "Shoulder",
            "Hair", "Eyebrow", "Lip", "Nose", "Chin", "Forehead", "Torso",
            // 직업/사람 관련 포괄적 단어
            "Model",
            // 표정/자세
            "Selfie", "Standing", "Sitting", "Gesture", "Happy", "Cool",
            // 배경/사물
            "Smartphone", "Mobile phone", "Phone",
            "Wall", "Floor", "Sky", "Room", "Building", "Background",
            "Furniture", "Photography", "Mirror",
            // 너무 포괄적인 단어 (모든 옷 사진에 붙어서 태그 가치가 없음)
            "Fashion", "Style", "Pattern", "Texture", "Design",
            // 도형/속성
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
                // AI의 다양성을 유지하되(화이트리스트 지양), 불확실한 쓰레기 태그를 막기 위해 70% 컷오프 유지
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
