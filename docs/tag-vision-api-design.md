# Tag 도메인 — Google Vision API 설계 및 구현 문서

> **작성/업데이트:** 2026-04-29
> **상태:** 1차 구현 완료 (추출 로직 고도화 완료)

---

## 1. 개요
게시물 작성(SCR-012) 및 시착 결과(SCR-024) 화면에서, 사용자가 업로드한 이미지를 **Google Cloud Vision API**로 분석하여 패션 관련 추천 태그를 자동 추출하는 기능입니다. 

**핵심 흐름:**
`이미지 업로드` → `태그 추출 API 호출` → `Vision API 다중 분석(LABEL+OBJECT)` → `화이트리스트 필터링` → `추천 태그 반환 (최대 10개)`

---

## 2. 설계 (Design)

### 2.1. 헥사고날 아키텍처
Tag 도메인은 외부 API(Google Vision)와의 강결합을 피하기 위해 어댑터 패턴을 철저히 적용합니다.
- **Inbound:** `TagController` (`POST /api/tags/analyze`)
- **Domain/Application:** `TagAnalysisService` (유효성 검증, 최대 10개 제한)
- **Outbound:** `VisionApiPort` (인터페이스) ↔ `GoogleVisionTagAdapter` (Vision API 실제 통신)

### 2.2. 태그 추출 및 필터링 전략 (핵심)
단순한 형태 분석의 한계를 극복하기 위해 **기능 다중 호출**과 **화이트리스트 방어막**을 사용합니다.

1. **다중 호출 (Dual Detection):**
   - `LABEL_DETECTION`: "Denim", "Fashion" 등 소재 및 전체적인 분위기 추출.
   - `OBJECT_LOCALIZATION`: "Top", "Pants", "Shoe" 등 구체적인 의류 객체(사물) 추출.
2. **화이트리스트(Whitelist) 필터링:**
   - 약 130개의 **패션 전용 키워드 사전**(`FASHION_WHITELIST`)에 등록된 단어만 통과시킵니다.
   - 배경(Furniture, Sky), 인물(Person, Face), 표정(Smile) 등의 쓰레기 노이즈가 타 도메인에 노출될 위험이 **0%**입니다.
3. **확신도(Confidence) 하향 최적화:**
   - 강력한 화이트리스트가 방어하므로, API 확신도 컷오프를 `0.5 (50%)`로 과감히 낮췄습니다. 이를 통해 AI가 놓치기 쉬운 세밀한 패션 아이템(`Pocket`, `Active Pants` 등)의 추출률을 대폭 끌어올렸습니다.

---

## 3. 구현 상태 (Implementation)

| 기능 | 상태 | 비고 |
|---|---|---|
| **API 엔드포인트** | ✅ 완료 | `POST /api/tags/analyze` (이미지 Multipart 받아서 태그 목록 JSON 응답) |
| **Vision API 연동** | ✅ 완료 | GCP 설정 및 `GoogleVisionTagAdapter` 구현 |
| **필터링 로직** | ✅ 완료 | 화이트리스트 사전 적용 및 정규식 단어 경계(`\b`) 매칭 적용 |
| **태그 제한** | ✅ 완료 | `TagAnalysisService`에서 최대 10개 제한 적용 |
| **보안 (ADC 인증)** | ✅ 완료 | 하드코딩 방지. (환경변수 `GOOGLE_APPLICATION_CREDENTIALS` 주입) |

---

## 4. 다음 단계 및 진행해야 할 업무 (Next Steps)

현재 브랜치(`feat/SCRUM-139` 등)를 포함하여 **앞으로 우리가 이어서 진행해야 하는 업무**입니다.

### 🎯 [우선순위 1] Post 도메인 연동 로직 구현 (진행 중)
현재 Tag 분석 API는 **"추천 태그 목록 반환"**까지만 수행합니다. 사용자가 이 추천 태그를 선택/수정한 후 게시물을 최종 업로드할 때, 태그를 DB에 저장하는 로직이 필요합니다.
- `posts` 테이블 저장 로직 구현 (현재 `SCRUM-139`에서 진행)
- `tags` 테이블에 신규 태그 Insert 및 `usage_count` 증가 (Upsert 로직)
- `post_tags` 테이블을 통한 N:M 연관관계 매핑 (게시물과 태그 연결)

### 🎯 [우선순위 2] 태그 사운드(언어) 매핑 및 검색 고도화
현재 Vision API는 영문 태그만 반환합니다. 서비스 품질 향상을 위한 확장이 필요할 수 있습니다.
- 영문 태그 그대로 사용할지, 한국어 매핑(영→한 사전)을 추가할지 프론트엔드/기획 팀과 최종 결정.
- 통합 검색(`GET /search`) 시 `#태그명`으로 게시물을 검색할 수 있는 조회 최적화 쿼리 작성.

### 💡 확장 가능성 (유지보수 가이드)
- **태그 사전 추가:** 특정 스타일(Y2K, Gorpcore 등)이나 브랜드를 분석 사전에 추가하고 싶다면, 백엔드 코드의 `GoogleVisionTagAdapter.java` 내 `FASHION_WHITELIST` 배열에 단어만 추가하면 즉시 반영됩니다.
- **성능 최적화 (Caching):** 트래픽이 증가할 경우, 완전히 동일한 이미지에 대해 중복된 Vision API 과금이 발생하지 않도록 이미지 해시값을 이용한 Redis 캐싱 도입을 고려할 수 있습니다.
