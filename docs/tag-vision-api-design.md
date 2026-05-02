# Tag 도메인 — Google Vision API 설계 및 구현 문서

> **작성/업데이트:** 2026-05-02
> **상태:** 2차 구현 완료 (피드 목록 조회 API + 홈 피드 페이지)

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

### 3.1. Tag 도메인 (Vision API)

| 기능 | 상태 | 비고 |
|---|---|---|
| **API 엔드포인트** | ✅ 완료 | `POST /api/tags/analyze` (이미지 Multipart 받아서 태그 목록 JSON 응답) |
| **Vision API 연동** | ✅ 완료 | GCP 설정 및 `GoogleVisionTagAdapter` 구현 |
| **필터링 로직** | ✅ 완료 | 화이트리스트 사전 적용 및 정규식 단어 경계(`\b`) 매칭 적용 |
| **태그 제한** | ✅ 완료 | `TagAnalysisService`에서 최대 10개 제한 적용 |
| **보안 (ADC 인증)** | ✅ 완료 | 하드코딩 방지. (환경변수 `GOOGLE_APPLICATION_CREDENTIALS` 주입) |
| **LoadTagsUseCase** | ✅ 완료 | Post → Tag 교차 도메인 Port 인터페이스 (`loadTagsByPostIds`) |

### 3.2. Post 도메인 (커뮤니티 피드)

| 기능 | 상태 | 비고 |
|---|---|---|
| **Post Entity/Repository** | ✅ 완료 | `Post`, `PostImage` 도메인 모델 + JPA 엔티티 + 영속성 어댑터 (SCRUM-139) |
| **게시물 작성 API** | ✅ 완료 | `POST /api/posts` — 이미지 5장 + 태그 10개 + 캡션 300자 (SCRUM-140) |
| **피드 목록 조회 API** | ✅ 완료 | `GET /api/posts` — 전체/팔로잉 탭, 최신순/인기순, 페이지네이션 (SCRUM-143) |
| **홈 피드 페이지** | ✅ 완료 | SwipeDeck + MainToggle + FeedGrid(무한 스크롤) + PostCard (SCRUM-156) |
| **feedAPI 클라이언트** | ✅ 완료 | `getFeedPosts`, `getPostDetail`, `createPost`, `toggleLike`, `createComment` |

---

## 4. 스프린트 로드맵

### ✅ 완료 (5건)

| 이슈 | ID | PR | 설명 |
|---|---|---|---|
| SCRUM-139 | FEED-001 | — | posts + post_images 테이블 Entity 및 Repository 구현 (8점) |
| SCRUM-141 | FEED-002a | — | 게시물 작성 시 Google Vision API 이미지 분석 연동 (8점) |
| SCRUM-140 | FEED-002 | #13 | 게시물 작성 API 구현 (4점) |
| SCRUM-143 | FEED-003 | #18 | 피드 목록 조회 API 구현 (4점) |
| SCRUM-156 | FEED-016 | #18 | 홈 피드 페이지 구현 (4점) |

### 📋 Backlog — 커뮤니티 피드 (FEED)

| 이슈 | ID | 설명 |
|---|---|---|
| SCRUM-142 | FEED-002b | Vision API 분석 결과 태그 추천 UI 구현 |
| SCRUM-144 | FEED-004 | 게시물 상세 조회 API 구현 |
| SCRUM-145 | FEED-005 | 게시물 삭제 API 구현 |
| SCRUM-146 | FEED-006 | 시착 직후 게시물 생성 연동 로직 구현 |
| SCRUM-147 | FEED-007 | likes 테이블 Entity 및 Repository 구현 |
| SCRUM-148 | FEED-008 | 좋아요 토글 API 구현 |
| SCRUM-149 | FEED-009 | comments 테이블 Entity 및 Repository 구현 |
| SCRUM-150 | FEED-010 | 댓글 목록 조회 API 구현 |
| SCRUM-151 | FEED-011 | 댓글 작성 API 구현 |
| SCRUM-152 | FEED-012 | 댓글 삭제 API 구현 |
| SCRUM-153 | FEED-013 | reports 테이블 Entity 및 Repository 구현 |
| SCRUM-154 | FEED-014 | 게시물 신고 API 구현 |
| SCRUM-155 | FEED-015 | 신고 N회 누적 자동 블라인드 트리거 구현 |
| SCRUM-157 | FEED-017 | 최신순/인기순 정렬 토글 구현 |
| SCRUM-158 | FEED-018 | 피드 무한 스크롤 및 이미지 Lazy Load |
| SCRUM-159 | FEED-019 | 게시물 상세 페이지 구현 |
| SCRUM-161 | FEED-021 | 좋아요 버튼 UI 및 낙관적 업데이트 구현 |
| SCRUM-162 | FEED-022 | 댓글 목록/작성/대댓글 UI 구현 |
| SCRUM-163 | FEED-023 | 게시물 신고 UI 구현 |
| SCRUM-164 | FEED-024 | 피드 CRUD API 클라이언트 연동 |
| SCRUM-165 | FEED-025 | 비로그인 사용자 피드 열람 및 로그인 유도 |
| SCRUM-166 | FEED-026 | 피드/좋아요/댓글/신고 단위 테스트 |
| SCRUM-167 | FEED-027 | 피드 페이지 UI 테스트 |

### 📋 Backlog — 태그 및 검색 (TAG)

| 이슈 | ID | 설명 |
|---|---|---|
| SCRUM-168 | TAG-001 | tags + post_tags 테이블 Entity 및 Repository 구현 |
| SCRUM-169 | TAG-002 | Google Vision API 태그 분석 서비스 구현 |
| SCRUM-170 | TAG-003 | 이미지 태그 자동 추출 API 구현 |
| SCRUM-171 | TAG-004 | 게시물 생성 시 태그 부착 서비스 로직 |
| SCRUM-172 | TAG-005 | 태그 자동완성 검색 API 구현 |
| SCRUM-173 | TAG-006 | 통합 검색 API 구현 |
| SCRUM-174 | TAG-007 | 게시물 작성 태그 자동완성 UI 구현 |
| SCRUM-175 | TAG-008 | 통합 검색 페이지 구현 |
| SCRUM-176 | TAG-009 | 피드 카드 태그 칩 표시 UI 구현 |
| SCRUM-177 | TAG-010 | 게시물 상세 태그 목록 UI 구현 |
| SCRUM-178 | TAG-011 | 태그/검색 API 클라이언트 연동 |
| SCRUM-179 | TAG-012 | 태그/검색 단위 테스트 및 UI 테스트 |

---

## 5. 다음 단계 상세 (Next Steps)

### 🎯 [다음] 게시물 상세 조회 API (SCRUM-144)
**백엔드:**
- `GET /api/posts/{postId}` 엔드포인트
- `is_liked` 필드 포함 (현재 `false` 하드코딩 → 실데이터 연동 필요)
- 조회수(`view_count`) 증가 로직

**프론트엔드:**
- 게시물 상세 페이지 (`app/(main)/post/[id]/page.tsx`)
- 이미지 캐러셀 + 태그 목록 + 댓글 UI

### 🎯 [다음] 좋아요 토글 API (SCRUM-147 → SCRUM-148)
**백엔드:**
- `likes` 테이블 Entity 및 Repository
- `POST /api/posts/{postId}/likes` 토글 API
- `PostService.getFeedPosts()`의 `isLiked` 실데이터 연동

**프론트엔드:**
- PostCard의 `toggleLike` API 호출 + 실패 시 롤백 로직 구현 (현재 TODO)

### 🎯 [보류] 팔로잉 탭 연동
- 현재 팔로잉 탭은 빈 리스트(`Collections.emptyList()`)로 호출됨
- Follow 도메인 구현 후 `PostService`에 팔로잉 유저 ID 목록 전달 필요

### 📝 구현 완료 후 발견된 개선 사항
- ~~`feedAPI.ts`의 `getPostDetail`, `toggleLike`, `createComment` 파라미터가 `number` 타입~~ → `string`(UUID)으로 수정 완료 (`fix/SCRUM-143`)
- ~~PR #18 머지 시 `next.config.ts`의 `/uploads` 이미지 프록시 `rewrites` 삭제됨~~ → 복원 완료 (`fix/SCRUM-143`)
- 정렬 토글 UI가 아직 없음 → SCRUM-157에서 구현 예정

---

## 6. 로컬 개발 및 보안 가이드 (Google Cloud Credentials)

Google Cloud Vision API는 인증(API Key) 없이 호출할 경우 서버 내부에서 `IOException`을 발생시키며 **500 Internal Server Error**를 반환합니다. 로컬에서 실제 API를 테스트하려면 다음 보안 및 세팅 가이드를 반드시 준수해야 합니다.

1. **보안 규칙 (Git 반영 금지):**
   - 구글 서비스 계정 키(JSON) 파일은 해킹 및 과금 방지를 위해 절대 GitHub 저장소에 올라가면 안 됩니다.
   - 프로젝트 최상단의 `.gitignore`에 `*-key.json`, `*credentials*.json` 규칙이 등록되어 있으므로, 파일명 끝을 `-key.json`으로 명명해야 합니다. (예: `simul-vision-key.json`)

2. **로컬 환경 세팅:**
   - 전달받은 JSON 키 파일을 `C:\team2-simul\backend\` 폴더 내에 위치시킵니다.
   - 프로젝트 최상단의 `.env` 파일에 아래 환경변수를 추가합니다.
     ```env
     GOOGLE_APPLICATION_CREDENTIALS=C:\team2-simul\backend\simul-vision-key.json
     ```
   - 백엔드 서버를 재시작(`.\run.bat`)하면, Spring Boot 구동 시 ADC(Application Default Credentials)가 해당 경로의 JSON 키를 읽어와 안전하게 구글 서버와 통신합니다.

---

## 7. 유지보수 가이드

- **태그 사전 추가:** 특정 스타일(Y2K, Gorpcore 등)이나 브랜드를 분석 사전에 추가하고 싶다면, `GoogleVisionTagAdapter.java` 내 `FASHION_WHITELIST` 배열에 단어만 추가하면 즉시 반영됩니다.
- **성능 최적화 (Caching):** 트래픽이 증가할 경우, 동일 이미지에 대한 중복 Vision API 과금 방지를 위해 이미지 해시값 기반 Redis 캐싱 도입을 고려할 수 있습니다.

---

## 8. 기능 검증 및 배포 프로세스 (Testing & Deployment Standard)

모든 신규 기능 추가는 다른 도메인에 영향을 주지 않고 안전하게 운영 서버에 반영되기 위해 다음 절차를 엄격히 준수합니다.

1. **기능 추가 및 단위/통합 테스트 (Local Testing)**
   - 백엔드: 프론트엔드나 타 도메인 연동을 기다리지 않고, `src/test/` 경로에 **통합 테스트 코드(Integration Test)**를 작성하여 자체 검증합니다.
   - 예: `@WithMockUser`나 `SecurityMockMvcRequestPostProcessors`를 이용해 로그인(인증) 상태를 모킹하고, `MockMvc`로 API 호출부터 DB 연동(PostgreSQL)까지 성공/실패 시나리오를 통과시킵니다.
   - 프론트엔드: `npm run dev` 환경에서 자체 목업 데이터나 API로 컴포넌트를 테스트합니다.

2. **Pull Request (PR) 및 코드 리뷰**
   - 로컬 테스트가 100% 통과되면, `feat/이슈번호` 브랜치를 GitHub에 Push하고 `develop` 브랜치를 향해 PR을 생성합니다.

3. **자동화된 빌드 및 배포 서버 확인 (CI/CD)**
   - PR이 `develop` 브랜치에 머지되면, 설정된 GitHub Actions 워크플로우(`.github/workflows/deploy.yml`)가 자동으로 트리거됩니다.
   - 자동화된 파이프라인이 `docker-compose up -d --build`를 배포 서버에서 실행하여 새로운 이미지를 배포합니다.
   - 배포된 개발 서버(Dev Server)에서 최종 E2E(End-to-End) 테스트를 수행하여 타 도메인과의 통합 여부를 확인합니다.
