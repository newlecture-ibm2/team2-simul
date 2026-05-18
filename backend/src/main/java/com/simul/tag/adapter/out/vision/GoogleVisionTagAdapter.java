package com.simul.tag.adapter.out.vision;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import com.simul.tag.application.port.out.VisionApiPort;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    // ──────────────────────────────────────────────────────────────
    // 색상 화이트리스트: 패션에서 유의미한 색상 키워드
    // 확신도 기준이 높음 (0.8f) — 이미지의 지배적 색상만 추출
    // ──────────────────────────────────────────────────────────────
    private static final Set<String> COLOR_WHITELIST = Set.of(
            "Black", "White", "Red", "Blue", "Navy", "Navy blue",
            "Green", "Yellow", "Pink", "Purple", "Orange",
            "Brown", "Beige", "Grey", "Gray", "Ivory",
            "Khaki", "Burgundy", "Maroon", "Coral",
            "Lavender", "Olive", "Teal", "Turquoise",
            "Gold", "Silver", "Cream", "Tan"
    );

    private static final Set<String> COLOR_WHITELIST_LOWER;

    static {
        var colorLowerSet = new java.util.HashSet<String>();
        for (String color : COLOR_WHITELIST) {
            colorLowerSet.add(color.toLowerCase());
        }
        COLOR_WHITELIST_LOWER = Set.copyOf(colorLowerSet);
    }

    // 화이트리스트 매칭 시 소문자 비교용 캐시
    private static final Set<String> FASHION_WHITELIST_LOWER;

    static {
        var lowerSet = new java.util.HashSet<String>();
        for (String keyword : FASHION_WHITELIST) {
            lowerSet.add(keyword.toLowerCase());
        }
        FASHION_WHITELIST_LOWER = Set.copyOf(lowerSet);
    }

    // ──────────────────────────────────────────────────────────────
    // 한국어 번역 매핑 사전: 화이트리스트 영어 → 트렌디한 한국어 패션 용어
    // 1:1 직역 원칙 (디테일 유지, 단순화 금지)
    // ──────────────────────────────────────────────────────────────
    private static final Map<String, String> KOREAN_TAG_DICTIONARY = Map.ofEntries(
            // ── 상의 (Tops) ──
            Map.entry("shirt", "셔츠"), Map.entry("t-shirt", "티셔츠"),
            Map.entry("blouse", "블라우스"), Map.entry("polo shirt", "폴로셔츠"),
            Map.entry("dress shirt", "드레스셔츠"), Map.entry("top", "상의"),
            Map.entry("crop top", "크롭탑"), Map.entry("tank top", "탱크탑"),
            Map.entry("camisole", "캐미솔"), Map.entry("tube top", "튜브탑"),
            Map.entry("sweater", "스웨터"), Map.entry("hoodie", "후드티"),
            Map.entry("cardigan", "가디건"), Map.entry("pullover", "풀오버"),
            Map.entry("sweatshirt", "맨투맨"), Map.entry("sleeveless shirt", "민소매"),
            Map.entry("active shirt", "액티브셔츠"), Map.entry("jersey", "저지"),

            // ── 하의 (Bottoms) ──
            Map.entry("pants", "팬츠"), Map.entry("jeans", "청바지"),
            Map.entry("trousers", "트라우저"), Map.entry("shorts", "반바지"),
            Map.entry("bermuda shorts", "버뮤다팬츠"), Map.entry("skirt", "스커트"),
            Map.entry("miniskirt", "미니스커트"), Map.entry("leggings", "레깅스"),
            Map.entry("cargo pants", "카고팬츠"), Map.entry("sweatpants", "트레이닝팬츠"),
            Map.entry("chinos", "치노팬츠"),

            // ── 아우터 (Outerwear) ──
            Map.entry("jacket", "재킷"), Map.entry("coat", "코트"),
            Map.entry("blazer", "블레이저"), Map.entry("parka", "파카"),
            Map.entry("windbreaker", "윈드브레이커"), Map.entry("leather jacket", "레더재킷"),
            Map.entry("denim jacket", "데님재킷"), Map.entry("bomber jacket", "봄버재킷"),
            Map.entry("trench coat", "트렌치코트"), Map.entry("overcoat", "오버코트"),
            Map.entry("puffer jacket", "패딩"), Map.entry("vest", "조끼"),
            Map.entry("raincoat", "레인코트"), Map.entry("poncho", "판초"),
            Map.entry("cape", "케이프"),

            // ── 원피스/세트 (Dresses & Sets) ──
            Map.entry("dress", "원피스"), Map.entry("day dress", "데이드레스"),
            Map.entry("cocktail dress", "칵테일드레스"), Map.entry("gown", "가운"),
            Map.entry("romper", "롬퍼"), Map.entry("jumpsuit", "점프수트"),
            Map.entry("overall", "오버올"), Map.entry("dungarees", "멜빵바지"),
            Map.entry("suit", "수트"), Map.entry("tuxedo", "턱시도"),
            Map.entry("uniform", "유니폼"),

            // ── 소재 (Materials) ──
            Map.entry("denim", "데님"), Map.entry("leather", "레더"),
            Map.entry("suede", "스웨이드"), Map.entry("silk", "실크"),
            Map.entry("satin", "새틴"), Map.entry("linen", "린넨"),
            Map.entry("cotton", "코튼"), Map.entry("wool", "울"),
            Map.entry("cashmere", "캐시미어"), Map.entry("velvet", "벨벳"),
            Map.entry("corduroy", "코듀로이"), Map.entry("tweed", "트위드"),
            Map.entry("flannel", "플란넬"), Map.entry("chiffon", "시폰"),
            Map.entry("lace", "레이스"), Map.entry("mesh", "메쉬"),
            Map.entry("nylon", "나일론"), Map.entry("polyester", "폴리에스터"),
            Map.entry("fleece", "플리스"), Map.entry("fur", "퍼"),
            Map.entry("faux fur", "인조퍼"), Map.entry("canvas", "캔버스"),

            // ── 패턴/무늬 (Patterns) ──
            Map.entry("plaid", "플레이드"), Map.entry("stripe", "스트라이프"),
            Map.entry("polka dot", "폴카도트"), Map.entry("floral", "플로럴"),
            Map.entry("paisley", "페이즐리"), Map.entry("houndstooth", "하운드투스"),
            Map.entry("camouflage", "카모플라주"), Map.entry("tartan", "타탄체크"),
            Map.entry("gingham", "깅엄체크"), Map.entry("checkered", "체크무늬"),
            Map.entry("argyle", "아가일"), Map.entry("animal print", "애니멀프린트"),
            Map.entry("tie-dye", "타이다이"),

            // ── 디테일/구조 (Details) ──
            Map.entry("collar", "카라"), Map.entry("pocket", "포켓"),
            Map.entry("sleeve", "슬리브"), Map.entry("button", "버튼"),
            Map.entry("zipper", "지퍼"), Map.entry("hood", "후드"),
            Map.entry("ruffle", "러플"), Map.entry("pleated", "플리츠"),
            Map.entry("embroidery", "자수"), Map.entry("sequin", "시퀸"),
            Map.entry("fringe", "프린지"), Map.entry("tassel", "태슬"),
            Map.entry("bow tie", "보타이"), Map.entry("lapel", "라펠"),
            Map.entry("cuff", "커프스"),

            // ── 신발 (Footwear) ──
            Map.entry("shoe", "슈즈"), Map.entry("sneakers", "스니커즈"),
            Map.entry("boots", "부츠"), Map.entry("sandals", "샌들"),
            Map.entry("loafer", "로퍼"), Map.entry("high heels", "하이힐"),
            Map.entry("slipper", "슬리퍼"), Map.entry("flip-flops", "플립플랍"),
            Map.entry("wedge shoe", "웨지슈즈"), Map.entry("oxford shoe", "옥스퍼드슈즈"),
            Map.entry("ankle boot", "앵클부츠"), Map.entry("knee-high boot", "니하이부츠"),
            Map.entry("running shoe", "러닝화"), Map.entry("athletic shoe", "운동화"),
            Map.entry("mule", "뮬"),

            // ── 가방 (Bags) ──
            Map.entry("bag", "가방"), Map.entry("handbag", "핸드백"),
            Map.entry("backpack", "백팩"), Map.entry("tote bag", "토트백"),
            Map.entry("clutch", "클러치"), Map.entry("crossbody bag", "크로스백"),
            Map.entry("shoulder bag", "숄더백"), Map.entry("wallet", "지갑"),
            Map.entry("purse", "퍼스"), Map.entry("luggage", "러기지"),
            Map.entry("briefcase", "브리프케이스"),

            // ── 악세서리 (Accessories) ──
            Map.entry("hat", "모자"), Map.entry("cap", "캡"),
            Map.entry("beanie", "비니"), Map.entry("beret", "베레모"),
            Map.entry("fedora", "페도라"), Map.entry("scarf", "스카프"),
            Map.entry("glove", "장갑"), Map.entry("belt", "벨트"),
            Map.entry("suspenders", "서스펜더"), Map.entry("sunglasses", "선글라스"),
            Map.entry("glasses", "안경"), Map.entry("watch", "시계"),
            Map.entry("wristwatch", "손목시계"), Map.entry("necklace", "목걸이"),
            Map.entry("bracelet", "팔찌"), Map.entry("earring", "귀걸이"),
            Map.entry("ring", "반지"), Map.entry("tie", "넥타이"),
            Map.entry("necktie", "넥타이"),

            // ── 속옷/홈웨어 (Innerwear & Loungewear) ──
            Map.entry("underwear", "속옷"), Map.entry("bra", "브라"),
            Map.entry("lingerie", "란제리"), Map.entry("swimsuit", "수영복"),
            Map.entry("bikini", "비키니"), Map.entry("pajamas", "파자마"),
            Map.entry("robe", "로브"), Map.entry("bathrobe", "목욕가운"),

            // ── 스타일 키워드 (Style) ──
            Map.entry("streetwear", "스트릿웨어"), Map.entry("formal wear", "포멀웨어"),
            Map.entry("sportswear", "스포츠웨어"), Map.entry("athleisure", "애슬레저"),
            Map.entry("vintage", "빈티지"), Map.entry("retro", "레트로"),
            Map.entry("casual", "캐주얼"), Map.entry("workwear", "워크웨어"),
            Map.entry("knitwear", "니트웨어"), Map.entry("outerwear", "아우터"),

            // ── 색상 (Colors) ──
            Map.entry("black", "블랙"), Map.entry("white", "화이트"),
            Map.entry("red", "레드"), Map.entry("blue", "블루"),
            Map.entry("navy", "네이비"), Map.entry("navy blue", "네이비"),
            Map.entry("green", "그린"), Map.entry("yellow", "옐로우"),
            Map.entry("pink", "핑크"), Map.entry("purple", "퍼플"),
            Map.entry("orange", "오렌지"), Map.entry("brown", "브라운"),
            Map.entry("beige", "베이지"), Map.entry("grey", "그레이"),
            Map.entry("gray", "그레이"), Map.entry("ivory", "아이보리"),
            Map.entry("khaki", "카키"), Map.entry("burgundy", "버건디"),
            Map.entry("maroon", "마룬"), Map.entry("coral", "코랄"),
            Map.entry("lavender", "라벤더"), Map.entry("olive", "올리브"),
            Map.entry("teal", "틸"), Map.entry("turquoise", "터콰이즈"),
            Map.entry("gold", "골드"), Map.entry("silver", "실버"),
            Map.entry("cream", "크림"), Map.entry("tan", "탄")
    );

    /**
     * 영어 태그를 한국어로 변환합니다.
     * 사전에 없는 경우 원본 영어 태그를 그대로 반환합니다.
     */
    private String translateToKorean(String englishTag) {
        String key = englishTag.toLowerCase();
        return KOREAN_TAG_DICTIONARY.getOrDefault(key, englishTag);
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

                if (isFashionTag(rawLabel) && rawScore >= 0.5f) {
                    String koreanTag = translateToKorean(rawLabel);
                    if (!recommendedTags.contains(koreanTag)) {
                        recommendedTags.add(koreanTag);
                    }
                } else if (isColorTag(rawLabel) && rawScore >= 0.8f) {
                    // 색상 태그는 확신도 80% 이상일 때만 (지배적 색상만 추출)
                    String koreanTag = translateToKorean(rawLabel);
                    if (!recommendedTags.contains(koreanTag)) {
                        recommendedTags.add(koreanTag);
                    }
                }
            }

            // 2. OBJECT_LOCALIZATION 처리 (구체적 사물: Top, Pants, Shoe 등)
            for (LocalizedObjectAnnotation annotation : res.getLocalizedObjectAnnotationsList()) {
                String rawLabel = annotation.getName();
                float rawScore = annotation.getScore();

                if (rawScore >= 0.5f && isFashionTag(rawLabel)) {
                    String koreanTag = translateToKorean(rawLabel);
                    if (!recommendedTags.contains(koreanTag)) {
                        recommendedTags.add(koreanTag);
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

    /**
     * 색상 태그인지 확인합니다.
     * 색상은 별도 화이트리스트로 관리하며, 확신도 기준이 더 높습니다 (0.8f).
     */
    private boolean isColorTag(String label) {
        return COLOR_WHITELIST_LOWER.contains(label.toLowerCase());
    }
}

