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
| **Post Entity/Repository** | ✅ 완료 | `Post`, `PostImage` 도메인 모델 + JPA 엔티티 + 영속성 어댑터 (SCRUM-139) |

---

## 4. 스프린트 로드맵

### 📅 2주차 — 설계 및 개발 (6개)

| 이슈 | ID | 상태 | 설명 |
|---|---|---|---|
| SCRUM-141 | FEED-002a | ✅ 완료 | 게시물 작성 시 Google Vision API 이미지 분석 연동 |
| SCRUM-139 | FEED-001 | ✅ 완료 | posts + post_images 테이블 Entity 및 Repository 구현 |
| SCRUM-140 | FEED-002 | 🔄 진행 중 | 게시물 작성 API + 게시물 작성 페이지 구현 |
| SCRUM-160 | FEED-020 | 🔄 진행 중 | ↑ SCRUM-140과 병합하여 진행 (백엔드 API + 프론트엔드 UI) |
| SCRUM-143 | FEED-003 | ⬚ 대기 | 피드 목록 조회 API + 홈 피드 페이지 구현 |
| SCRUM-156 | FEED-016 | ⬚ 대기 | ↑ SCRUM-143과 병합하여 진행 (백엔드 API + 프론트엔드 UI) |

### 📋 Backlog — FEED 도메인 (23개)

| 이슈 | ID | 설명 |
|---|---|---|
| SCRUM-142 | FEED-002b | 게시물 작성 시 선택한 이미지 태그(옷 정보) 저장 |
| SCRUM-144 | FEED-004 | 특정 게시물 상세 조회 API |
| SCRUM-145 | FEED-005 | 게시물 수정 API |
| SCRUM-146 | FEED-006 | 게시물 삭제 API |
| SCRUM-147 | FEED-007 | 댓글 작성 API |
| SCRUM-148 | FEED-008 | 댓글 목록 조회 API |
| SCRUM-149 | FEED-009 | 댓글 삭제 API |
| SCRUM-150 | FEED-010 | 좋아요 등록/취소 API |
| SCRUM-151 | FEED-011 | 게시물 신고 API |
| SCRUM-152 | FEED-012 | 게시물 검색 API (태그, 작성자 기준) |
| SCRUM-153 | FEED-013 | 게시물 필터링 조회 API |
| SCRUM-154 | FEED-014 | 대댓글(Nested Comments) 기능 |
| SCRUM-155 | FEED-015 | 인기 게시물(Top Posts) 선정 로직 및 조회 API |
| SCRUM-157 | FEED-017 | 게시물 상세 페이지 |
| SCRUM-158 | FEED-018 | 댓글 UI 및 기능 |
| SCRUM-159 | FEED-019 | 좋아요 기능 |
| SCRUM-161 | FEED-021 | 게시물 수정/삭제 페이지 |
| SCRUM-162 | FEED-022 | 게시물 검색 및 결과 페이지 |
| SCRUM-163 | FEED-023 | 사진 다중 업로드 캐러셀 UI |
| SCRUM-164 | FEED-024 | 무한 스크롤(Infinite Scroll) 적용 |
| SCRUM-165 | FEED-025 | 해시태그 기반 태깅 시스템 UI |

### 📋 Backlog — MYP(마이페이지) 도메인 (14개)

| 이슈 | ID | 설명 |
|---|---|---|
| SCRUM-166 | MYP-001 | user 테이블 Entity 및 Repository 구현 |
| SCRUM-167 | MYP-002 | 프로필 정보 조회 API |
| SCRUM-168 | MYP-003 | 프로필 정보 수정 API |
| SCRUM-169 | MYP-004 | 마이페이지(내가 쓴 글) 조회 API |
| SCRUM-170 | MYP-005 | 팔로우/팔로워 목록 조회 API |
| SCRUM-171 | MYP-006 | 팔로우 신청/취소 API |
| SCRUM-172 | MYP-007 | 북마크(저장한 게시물) 조회 API |
| SCRUM-173 | MYP-008 | 회원 탈퇴 API |
| SCRUM-174 | MYP-009 | 마이페이지 프로필 UI |
| SCRUM-175 | MYP-010 | 팔로우/팔로잉 관리 페이지 |
| SCRUM-176 | MYP-011 | 내가 저장한 게시물 목록 페이지 |
| SCRUM-177 | MYP-012 | 프로필 수정(이미지, 닉네임 등) 기능 |
| SCRUM-178 | MYP-013 | 활동 로그(알림 등) 기본 기능 |
| SCRUM-179 | MYP-014 | 계정 보안 설정 관리 페이지 |

---

## 5. 다음 단계 상세 (Next Steps)

### 🎯 [현재] 게시물 작성 기능 (SCRUM-140 + SCRUM-160)
**백엔드:**
- `POST /api/posts` 엔드포인트 (PostController, PostService)
- 이미지 최대 5장 로컬 스토리지(`/uploads/images/`) 저장
- `tags` 테이블 Upsert + `post_tags` N:M 매핑

**프론트엔드:**
- 게시물 작성 페이지 (`app/(main)/post/create/page.tsx`)
- 다중 이미지 캐러셀 UI + 태그 Chip 편집 + 캡션 300자 제한

### 🎯 [다음] 홈 피드 조회 기능 (SCRUM-143 + SCRUM-156)
**백엔드:**
- `GET /api/posts` 엔드포인트 (전체/팔로잉 탭, 최신순/인기순 정렬, 페이지네이션)

**프론트엔드:**
- 홈 피드 페이지 (`app/(main)/page.tsx`)
- 2열 그리드 + 무한 스크롤 + PostCard 컴포넌트

### 🎯 [우선순위 3] 태그 검색 고도화
- 영문 태그 그대로 사용할지, 한국어 매핑(영→한 사전)을 추가할지 프론트엔드/기획 팀과 최종 결정.
- 통합 검색(`GET /search`) 시 `#태그명`으로 게시물을 검색할 수 있는 조회 최적화 쿼리 작성.

---

## 6. 유지보수 가이드

- **태그 사전 추가:** 특정 스타일(Y2K, Gorpcore 등)이나 브랜드를 분석 사전에 추가하고 싶다면, `GoogleVisionTagAdapter.java` 내 `FASHION_WHITELIST` 배열에 단어만 추가하면 즉시 반영됩니다.
- **성능 최적화 (Caching):** 트래픽이 증가할 경우, 동일 이미지에 대한 중복 Vision API 과금 방지를 위해 이미지 해시값 기반 Redis 캐싱 도입을 고려할 수 있습니다.
