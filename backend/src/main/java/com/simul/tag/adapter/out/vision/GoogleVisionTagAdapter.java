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
 * Google Cloud Vision API와 직접 통신하며, 화이트리스트(패션 허용 사전) 기반으로 필터링합니다.
 *
 * 전략: 화이트리스트(Whitelist) 방식
 * - AI가 분석한 모든 라벨 중, 패션 사전(FASHION_WHITELIST)에 포함된 단어만 통과시킵니다.
 * - 배경/인체/사물 등 비패션 노이즈가 아무리 높은 점수를 받아도 사전에 없으면 자동 차단됩니다.
 * - 덕분에 confidence 기준을 낮춰도(0.5f) 안전하게 다양한 패션 태그를 수집할 수 있습니다.
 */
@Component
public class GoogleVisionTagAdapter implements VisionApiPort {

    // ──────────────────────────────────────────────────────────────
    // 화이트리스트: 이 사전에 포함된 패션 관련 단어만 최종 태그로 허용
    // Google Vision API의 LABEL_DETECTION이 반환하는 영어 라벨 기준
    // 팀 조율을 통해 지속적으로 보완 (추가/삭제 가능)
    // ──────────────────────────────────────────────────────────────
    private static final Set<String> FASHION_WHITELIST = Set.of(

            // ── 상의 (Tops) ──
            "Shirt", "T-shirt", "Blouse", "Polo shirt", "Dress shirt",
            "Top", "Crop top", "Tank top", "Camisole", "Tube top",
            "Sweater", "Hoodie", "Cardigan", "Pullover", "Sweatshirt",
            "Sleeveless shirt", "Active shirt", "Jersey",

            // ── 하의 (Bottoms) ──
            "Pants", "Jeans", "Trousers", "Shorts", "Bermuda shorts",
            "Skirt", "Miniskirt", "Leggings", "Cargo pants",
            "Sweatpants", "Chinos",

            // ── 아우터 (Outerwear) ──
            "Jacket", "Coat", "Blazer", "Parka", "Windbreaker",
            "Leather jacket", "Denim jacket", "Bomber jacket",
            "Trench coat", "Overcoat", "Puffer jacket", "Vest",
            "Raincoat", "Poncho", "Cape",

            // ── 원피스/세트 (Dresses & Sets) ──
            "Dress", "Day dress", "Cocktail dress", "Gown",
            "Romper", "Jumpsuit", "Overall", "Dungarees",
            "Suit", "Tuxedo", "Uniform",

            // ── 소재 (Materials) ──
            "Denim", "Leather", "Suede", "Silk", "Satin",
            "Linen", "Cotton", "Wool", "Cashmere", "Velvet",
            "Corduroy", "Tweed", "Flannel", "Chiffon",
            "Lace", "Mesh", "Nylon", "Polyester", "Fleece",
            "Fur", "Faux fur", "Canvas",

            // ── 패턴/무늬 (Patterns) ──
            "Plaid", "Stripe", "Polka dot", "Floral",
            "Paisley", "Houndstooth", "Camouflage", "Tartan",
            "Gingham", "Checkered", "Argyle", "Animal print",
            "Tie-dye",

            // ── 디테일/구조 (Details) ──
            "Collar", "Pocket", "Sleeve", "Button",
            "Zipper", "Hood", "Ruffle", "Pleated",
            "Embroidery", "Sequin", "Fringe", "Tassel",
            "Bow tie", "Lapel", "Cuff",

            // ── 신발 (Footwear) ──
            "Shoe", "Sneakers", "Boots", "Sandals", "Loafer",
            "High heels", "Slipper", "Flip-flops", "Wedge shoe",
            "Oxford shoe", "Ankle boot", "Knee-high boot",
            "Running shoe", "Athletic shoe", "Mule",

            // ── 가방 (Bags) ──
            "Bag", "Handbag", "Backpack", "Tote bag",
            "Clutch", "Crossbody bag", "Shoulder bag",
            "Wallet", "Purse", "Luggage", "Briefcase",

            // ── 악세서리 (Accessories) ──
            "Hat", "Cap", "Beanie", "Beret", "Fedora",
            "Scarf", "Glove", "Belt", "Suspenders",
            "Sunglasses", "Glasses", "Watch", "Wristwatch",
            "Necklace", "Bracelet", "Earring", "Ring",
            "Tie", "Necktie",

            // ── 속옷/홈웨어 (Innerwear & Loungewear) ──
            "Underwear", "Bra", "Lingerie", "Swimsuit", "Bikini",
            "Pajamas", "Robe", "Bathrobe",

            // ── 스타일 키워드 (Style) ──
            "Streetwear", "Formal wear", "Sportswear", "Athleisure",
            "Vintage", "Retro", "Casual", "Workwear",
            "Knitwear", "Outerwear"
    );

    // 화이트리스트 매칭 시 소문자 비교용 캐시
    private static final Set<String> FASHION_WHITELIST_LOWER;

    static {
        var lowerSet = new java.util.HashSet<String>();
        for (String keyword : FASHION_WHITELIST) {
            lowerSet.add(keyword.toLowerCase());
        }
        FASHION_WHITELIST_LOWER = Set.copyOf(lowerSet);
    }

    @Override
    public List<String> analyzeImage(MultipartFile file) {
        List<String> recommendedTags = new ArrayList<>();

        try (ImageAnnotatorClient visionClient = ImageAnnotatorClient.create()) {

            ByteString imgBytes = ByteString.readFrom(file.getInputStream());
            Image image = Image.newBuilder().setContent(imgBytes).build();

            Feature labelFeature = Feature.newBuilder()
                    .setType(Feature.Type.LABEL_DETECTION)
                    .setMaxResults(30)  // 화이트리스트 방식이므로 후보를 넉넉히 요청
                    .build();

            Feature objectFeature = Feature.newBuilder()
                    .setType(Feature.Type.OBJECT_LOCALIZATION)
                    .setMaxResults(15)  // 사진 내의 구체적인 사물(옷 등) 탐색
                    .build();

            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(labelFeature)
                    .addFeatures(objectFeature)
                    .setImage(image)
                    .build();

            BatchAnnotateImagesResponse response = visionClient.batchAnnotateImages(List.of(request));
            AnnotateImageResponse res = response.getResponses(0);

            if (res.hasError()) {
                throw new RuntimeException("ERR-307-B: Vision API 오류 - " + res.getError().getMessage());
            }

            // 1. LABEL_DETECTION 처리 (추상적, 분위기, 소재 등)
            for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
                String rawLabel = annotation.getDescription();
                float rawScore = annotation.getScore();

                if (rawScore >= 0.5f && isFashionTag(rawLabel)) {
                    if (!recommendedTags.contains(rawLabel)) {
                        recommendedTags.add(rawLabel);
                    }
                }
            }

            // 2. OBJECT_LOCALIZATION 처리 (구체적 사물: Top, Pants, Shoe 등)
            for (LocalizedObjectAnnotation annotation : res.getLocalizedObjectAnnotationsList()) {
                String rawLabel = annotation.getName();
                float rawScore = annotation.getScore();

                if (rawScore >= 0.5f && isFashionTag(rawLabel)) {
                    if (!recommendedTags.contains(rawLabel)) {
                        recommendedTags.add(rawLabel);
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("ERR-307-B: 이미지 분석 중 오류 발생", e);
        }

        return recommendedTags;
    }

    private boolean isFashionTag(String label) {
        String lowerLabel = label.toLowerCase();

        // 1. 정확히 일치하는 경우 (예: "Denim" == "denim")
        if (FASHION_WHITELIST_LOWER.contains(lowerLabel)) {
            return true;
        }

        // 2. 부분 매칭: 라벨 안에 화이트리스트 단어가 포함된 경우 (단어 경계 일치)
        //    (예: "Sleeveless shirt"는 "shirt"를 포함하지만, "Furniture"는 "fur"를 포함하더라도 독립 단어가 아니므로 거름)
        for (String keyword : FASHION_WHITELIST_LOWER) {
            if (lowerLabel.matches(".*\\b" + keyword + "\\b.*")) {
                return true;
            }
        }

        return false;
    }
}

