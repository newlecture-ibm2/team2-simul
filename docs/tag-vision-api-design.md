# Tag 도메인 — Google Vision API 연동 설계서

> **작성일:** 2026-04-29  
> **담당:** SCRUM-141 (게시물 작성 시 Google Vision API 이미지 분석 연동)  
> **상태:** 1차 구현 완료, 팀 조율 후 필터링 로직 고도화 예정

---

## 1. 개요

게시물 작성(SCR-012) 및 시착 결과(SCR-024) 화면에서, 사용자가 업로드한 이미지를 **Google Cloud Vision API**로 분석하여 패션 관련 추천 태그를 자동 추출하는 기능입니다.

### 핵심 흐름
```
사용자 이미지 업로드
  → POST /api/tags/analyze (TagController)
  → TagAnalysisService (유효성 검증 + 태그 10개 제한)
  → GoogleVisionTagAdapter (Vision API 통신 + 필터링)
  → 추천 태그 JSON 응답
```

---

## 2. 아키텍처 (헥사고날)

```
tag/
├── adapter/
│   ├── in/web/
│   │   └── TagController.java            ← POST /api/tags/analyze
│   └── out/vision/
│       └── GoogleVisionTagAdapter.java    ← Google Vision API 통신
└── application/
    ├── port/
    │   ├── in/AnalyzeImageTagsUseCase.java   ← Inbound Port (인터페이스)
    │   └── out/VisionApiPort.java             ← Outbound Port (인터페이스)
    ├── service/
    │   └── TagAnalysisService.java            ← 비즈니스 로직 (UseCase 구현체)
    └── dto/                                    ← (향후) Command, Response DTO
```

### 의존 방향
- `Controller` → `AnalyzeImageTagsUseCase`(Port) ← `TagAnalysisService`(Service) → `VisionApiPort`(Port) ← `GoogleVisionTagAdapter`(Adapter)
- 도메인 로직은 외부 인프라(Google)에 의존하지 않음
- Adapter 교체만으로 다른 Vision 서비스(Naver, AWS 등)로 전환 가능

---

## 3. API 스펙

### `POST /api/tags/analyze`

| 항목 | 값 |
|---|---|
| Content-Type | `multipart/form-data` |
| Request Body | `image`: 이미지 파일 (JPG/PNG/HEIC, 최대 20MB) |

#### 성공 응답 (200)
```json
{
  "recommended_tags": ["Sleeveless shirt", "Denim", "Active Shirt", "Pocket"],
  "count": 4
}
```

#### 실패 응답 (500, ERR-307-B)
```json
{
  "error_code": "ERR-307-B",
  "message": "자동 태그 추출에 실패했어요. 수동으로 입력해주세요.",
  "detail": "네트워크/파일 입출력 오류 상세"
}
```

---

## 4. 현재 필터링 정책

### 4.1 Confidence 기준
- **0.7 (70%) 이상**인 라벨만 통과
- 근거: 0.5로 낮추면 `Abdomen`, `Bangs`, `Photo shoot` 등 노이즈 태그가 대량 유입됨 (테스트 확인 완료)

### 4.2 블랙리스트 (현재 적용 중)
구글 Vision API가 70% 이상 확신하더라도, 패션과 명백히 무관한 단어를 제거합니다.

| 카테고리 | 제외 단어 |
|---|---|
| 인체 부위 | Person, People, Human, Face, Smile, Skin, Head, Hand, Arm, Leg, Waist, Thigh, Neck, Shoulder, Hair, Eyebrow, Lip, Nose, Chin, Forehead, Torso |
| 직업/사람 | Model |
| 표정/자세 | Selfie, Standing, Sitting, Gesture, Happy, Cool |
| 배경/사물 | Smartphone, Mobile phone, Phone, Wall, Floor, Sky, Room, Building, Background, Furniture, Photography, Mirror |
| 포괄적 단어 | Fashion, Style, Pattern, Texture, Design |
| 도형/속성 | Rectangle, Font, Circle, Material property |

### 4.3 태그 상한
- 최대 **10개** (ERR-307-A 정책, `TagAnalysisService`에서 처리)

---

## 5. 테스트 결과 기록

### 테스트 환경
- Google Cloud 프로젝트: `amazing-craft-494703-s8`
- 인증: 서비스 계정 JSON Key (환경변수 `GOOGLE_APPLICATION_CREDENTIALS`)
- 도구: Postman → `POST http://localhost:8080/api/tags/analyze`

### 테스트 케이스

| # | 이미지 유형 | 추출 결과 | 비고 |
|---|---|---|---|
| 1 | 데님 셔츠 (깔끔한 배경) | `Sleeve, Denim, Fashion, Waist, Active Shirt, Pocket` (6개) | Fashion, Waist가 노이즈 → 블랙리스트 추가 |
| 2 | 민소매 + 레이어드 스커트 | `Sleeveless shirt, Model, Torso` (3개) | Model, Torso가 노이즈 → 블랙리스트 추가 |
| 3 | 교실 환경 (복잡한 배경) | 다수의 배경 사물 태그 예상 | 배경 노이즈 필터링의 한계 노출 (블랙리스트 한계) |
| 4 | 사람 전신 (배경 없음) | `추출된 태그 0개` (빈 배열) | 옷 태그 점수가 70% 미만이라 탈락 + 고득점 인물 태그는 블랙리스트로 탈락 |

---

## 6. ⚠️ 팀 조율 필요 사항 (TODO)

### 6.1 필터링 전략 결정 (최우선)

현재 **블랙리스트(제외) 방식 + 70% Cut-off 방식**의 치명적인 한계가 확인되었습니다.

**한계 1 (노이즈 과다):** 배경이 복잡한 사진에서는 블랙리스트에 없는 새로운 비패션 단어(`Monitor`, `Desk`, `Cable` 등)가 계속 뚫고 나옵니다. 세상 모든 사물을 차단할 수 없습니다.
**한계 2 (빈 깡통 현상):** 사진에 인물이 도드라지면 AI는 `Person`(99%), `Hair`(90%)에 고득점을 주고, `Denim`, `Shirt` 등 진짜 패션 태그에는 60% 대의 낮은 점수를 줍니다. 이때 70% 컷오프를 적용하면 **정작 중요한 옷 태그는 짤리고, 고득점 인물 태그는 블랙리스트에 걸려 결국 추출 태그가 0개가 되는 현상**이 발생합니다.

**대안: 화이트리스트(허용 사전) 방식 + 컷오프 점수 하향 (40~50%)**
- 약 100~200개의 패션 전용 키워드 사전(Whitelist)을 구축합니다.
- 점수 컷오프를 40%~50% 정도로 과감히 낮춰서, 낮은 확률 대역에 숨어있는 '진짜 옷 태그'들을 넉넉히 건져 올립니다.
- 어차피 화이트리스트(패션 단어장)에 없는 `Abdomen`, `Monitor`, `Desk` 같은 쓰레기 단어는 자동 차단되므로 노이즈 유입 위험이 없습니다.
- **결론:** AI의 분석력(눈)은 100% 활용하면서, 필터링 로직(패션 뇌)은 우리가 안전하게 통제하는 최적의 구조입니다.

> **결정이 필요한 이유:** 화이트리스트에 어떤 단어를 넣을지(카테고리, 소재, 스타일, 브랜드 등)는 팀 전체의 서비스 방향성과 직결되므로 팀원 합의 필요

### 6.2 태그 언어 정책

현재 Google Vision API는 **영어 태그**만 반환합니다.

| 옵션 | 장점 | 단점 |
|---|---|---|
| A. 영어 태그 그대로 사용 | 구현 비용 0, 글로벌 확장성 | 한국어 사용자 친화도 낮음 |
| B. 영→한 번역 사전 매핑 | 사용자 친화적 | 매핑 사전 관리 필요 |
| C. 영어+한국어 병기 | 검색성 극대화 | DB 구조 확장 필요 |

### 6.3 태그 upsert 로직 (Tag + PostTag 테이블 연동)

현재 API는 추천 태그 **목록 반환**까지만 구현되어 있습니다.
게시물 작성 시 확정된 태그를 `tags` + `post_tags` 테이블에 저장하는 로직은 **Post 도메인(SCRUM-140)** 구현 시 함께 개발 예정입니다.

```
[현재] 이미지 → Vision API → 추천 태그 목록 반환 (여기까지 완료)
[향후] 사용자가 태그 선택/편집 → 게시물 저장 시 tags + post_tags에 upsert
```

### 6.4 Vision API 호출 최적화 (향후)

| 항목 | 현재 | 개선안 |
|---|---|---|
| API 호출 시점 | 매 요청마다 새 클라이언트 생성 | 싱글턴 Bean으로 재사용 |
| 응답 캐싱 | 없음 | 동일 이미지 해시 기준 캐싱 |
| 비용 관리 | 무료 크레딧 사용 중 | 월 1,000건 무료 한도 모니터링 필요 |

---

## 7. 관련 문서 및 코드

| 항목 | 경로 |
|---|---|
| API 스펙 | `docs/simul-api-spec.md` → `POST /tags/analyze` |
| ERD | `docs/simul-erd.md` → `tags`, `post_tags` 테이블 |
| 기능 명세 | `docs/simul-functional-spec.md` → FN-307 |
| 에러 코드 | ERR-307-A (태그 10개 초과), ERR-307-B (Vision API 실패) |
| 백엔드 코드 | `backend/src/main/java/com/simul/tag/` |
| GCP 프로젝트 | Google Cloud Console → `amazing-craft-494703-s8` |

---

## 8. 변경 이력

| 날짜 | 내용 |
|---|---|
| 2026-04-28 | 헥사고날 아키텍처 기반 태그 분석 백엔드 스켈레톤 구현 |
| 2026-04-28 | GCP 서비스 계정 발급 및 Cloud Vision API 사용 설정 |
| 2026-04-29 | Postman 실제 통신 테스트 성공, 블랙리스트 1차 보완 (Waist, Fashion, Model, Torso 추가) |
| 2026-04-29 | Confidence 기준 0.5 → 0.7 롤백 (노이즈 태그 과다 유입 확인) |
| 2026-04-29 | 필터링 전략 전환(블랙리스트→화이트리스트) 팀 조율 항목으로 등록 |
