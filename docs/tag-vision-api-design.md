# Tag 도메인 — Google Vision API 설계 및 구현 문서

> **작성/업데이트:** 2026-05-11
> **상태:** 5차 구현 완료 (게시물 상세/수정/삭제 CRUD, 좋아요 토글, 정렬 연동, BFF 데이터 매핑 버그 픽스, 대표 이미지 누락 버그 픽스)
> **비고:** 배포 서버에서의 이미지 DB 볼륨 유지 및 Vision API 연동 동작은 추가 검증 대기 상태 (검증 필요 항목으로 마킹됨)

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

### 2.3. 데이터 삭제 정책 (예외 규정)
일반적으로 `conventions.md`에 따라 모든 엔티티는 `BaseEntity`를 상속하여 Soft Delete(`deleted_at`)를 수행해야 하지만, **매핑 테이블(N:M 관계) 및 로그성 데이터에는 예외를 적용**합니다. 이는 Soft Delete로 인해 발생하는 Unique Constraint 충돌(예: 동일 게시물-태그 재매핑 시 에러)을 방지하기 위함입니다.
- **주체 엔티티** (posts, comments 등): Soft Delete (`BaseJpaEntity` 상속 ✅)
- **매핑 테이블** (post_tags, likes, follows, reports): **Hard Delete** (물리 삭제) (`BaseJpaEntity` 상속 ❌, `created_at`만 직접 관리)
- **미디어 리소스** (post_images 등): Soft Delete (파일 참조 보전)

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
| **게시물 상세/수정/삭제 API** | ✅ 완료 | `GET /posts/{id}`, `PATCH /posts/{id}`, `DELETE /posts/{id}` 완전한 CRUD 지원 (SCRUM-144, 145) |
| **홈 피드 페이지** | ✅ 완료 | SwipeDeck + MainToggle + FeedGrid(무한 스크롤) + PostCard (SCRUM-156) |
| **게시물 상세/수정 페이지** | ✅ 완료 | 이미지 캐러셀(PC 드래그 지원), 태그/캡션 수정, 삭제 토글, 모바일 하이라이트 개선 (SCRUM-145) |
| **feedAPI 클라이언트** | ✅ 완료 | `getFeedPosts`, `getPostDetail`, `createPost`, `updatePost`, `deletePost`, `toggleLike` |

---

## 4. 스프린트 로드맵

### ✅ 완료 (14건)

| 이슈 | ID | PR | 설명 |
|---|---|---|---|
| SCRUM-139 | FEED-001 | — | posts + post_images 테이블 Entity 및 Repository 구현 (8점) |
| SCRUM-141 | FEED-002a | — | 게시물 작성 시 Google Vision API 이미지 분석 연동 (8점) |
| SCRUM-140 | FEED-002 | #13 | 게시물 작성 API 구현 (4점) |
| SCRUM-143 | FEED-003 | #18 | 피드 목록 조회 API 구현 (4점) |
| SCRUM-156 | FEED-016 | #18 | 홈 피드 페이지 구현 (4점) |
| SCRUM-144 | FEED-004 | #75 | 게시물 상세 조회 API 구현 |
| SCRUM-145 | FEED-005 | #75 | 게시물 수정 및 삭제(Soft Delete) API 및 UI 구현 |
| SCRUM-159 | FEED-019 | #75 | 게시물 상세 페이지 API 연동 및 UI 구현 |
| SCRUM-164 | FEED-024 | #75 | 피드 CRUD API 클라이언트 연동 완료 |
| SCRUM-147 | FEED-007 | #75 | likes 테이블 Entity 및 Repository 구현 |
| SCRUM-148 | FEED-008 | #75 | 좋아요 토글 API 구현 (`POST /api/posts/{postId}/likes`) |
| SCRUM-161 | FEED-021 | #75 | 좋아요 버튼 UI 및 낙관적 업데이트 로직 적용 |
| SCRUM-157 | FEED-017 | #75 | 최신순/인기순 정렬 토글 연동 |
| SCRUM-177 | TAG-010 | #75 | 게시물 상세 페이지 태그 목록 UI 연동 |

### 📋 Backlog — 커뮤니티 피드 (FEED)

| 이슈 | ID | 설명 |
|---|---|---|
| SCRUM-142 | FEED-002b | Vision API 분석 결과 태그 추천 UI 구현 |
| SCRUM-146 | FEED-006 | 시착 직후 게시물 생성 연동 로직 구현 |
| SCRUM-149 | FEED-009 | comments 테이블 Entity 및 Repository 구현 (완료) |
| SCRUM-150 | FEED-010 | 댓글 목록 조회 API 구현 (완료) |
| SCRUM-151 | FEED-011 | 댓글 작성 API 구현 (완료) |
| SCRUM-152 | FEED-012 | 댓글 삭제 API 구현 (완료) |
| SCRUM-153 | FEED-013 | reports 테이블 Entity 및 Repository 구현 (완료) |
| SCRUM-154 | FEED-014 | 게시물 신고 API 구현 (완료) |
| SCRUM-155 | FEED-015 | 신고 N회 누적 자동 블라인드 트리거 구현 (완료) |
| SCRUM-162 | FEED-022 | 댓글 목록/작성/대댓글 UI 구현 (완료) |
| SCRUM-163 | FEED-023 | 게시물 신고 UI 구현 (완료) |
| SCRUM-165 | FEED-025 | 비로그인 사용자 피드 열람 및 로그인 유도 |
| SCRUM-166 | FEED-026 | 피드/좋아요/댓글/신고 단위 테스트 |
| SCRUM-167 | FEED-027 | 피드 페이지 UI 테스트 |

### 📋 Backlog — 태그 및 검색 (TAG)

| 이슈 | ID | 설명 |
|---|---|---|
| ~~SCRUM-168~~ | ~~TAG-001~~ | ~~tags + post_tags 테이블 Entity 및 Repository 구현~~ ✅ 구현 완료 |
| ~~SCRUM-169~~ | ~~TAG-002~~ | ~~Google Vision API 태그 분석 서비스 구현~~ ✅ 구현 완료 |
| ~~SCRUM-170~~ | ~~TAG-003~~ | ~~이미지 태그 자동 추출 API 구현~~ ✅ 구현 완료 |
| ~~SCRUM-171~~ | ~~TAG-004~~ | ~~게시물 생성 시 태그 부착 서비스 로직~~ ✅ 구현 완료 |
| SCRUM-172 | TAG-005 | 태그 자동완성 검색 API 구현 |
| SCRUM-173 | TAG-006 | 통합 검색 API 구현 |
| SCRUM-174 | TAG-007 | 게시물 작성 태그 자동완성 UI 구현 |
| SCRUM-175 | TAG-008 | 통합 검색 페이지 구현 |
| ~~SCRUM-176~~ | ~~TAG-009~~ | ~~피드 카드 태그 칩 표시 UI 구현~~ ✅ 구현 완료 |
| ~~SCRUM-177~~ | ~~TAG-010~~ | ~~게시물 상세 태그 목록 UI 구현~~ ✅ 완료 (#75) |
| SCRUM-178 | TAG-011 | 태그/검색 API 클라이언트 연동 |
| SCRUM-179 | TAG-012 | 태그/검색 단위 테스트 및 UI 테스트 |

---

## 5. 남은 기능 구현 계획 (풀스택 통합 브랜치 전략)

> **기준일:** 2026-05-11 ~ | 브랜치와 PR 횟수를 최소화하기 위해 **기능 단위 풀스택**으로 묶어 진행합니다.
> **전략:** 하나의 Feature Branch에서 백엔드(Entity → API)와 프론트엔드(UI → 연동)를 모두 완성한 뒤 PR 1건으로 올립니다.

### 📌 사전 정리 — 이미 구현된 이슈 (Jira Done 처리)

코드 확인 결과 아래 항목들은 이미 구현되어 있으므로, 별도 작업 없이 Jira에서 Done 처리합니다.

| 이슈 | 설명 | 근거 |
|---|---|---|
| SCRUM-168 (TAG-001) | tags + post_tags Entity/Repository | `Tag.java`, `PostTag.java`, `TagPersistenceAdapter.java` 존재 |
| SCRUM-169 (TAG-002) | Vision API 태그 분석 서비스 | `TagAnalysisService.java`, `GoogleVisionTagAdapter.java` 존재 |
| SCRUM-170 (TAG-003) | 이미지 태그 자동 추출 API | `TagController` → `POST /api/tags/analyze` 존재 |
| SCRUM-171 (TAG-004) | 게시물 생성 시 태그 부착 | `AttachTagsToPostService.java` → `PostService.createPost()`에서 호출 중 |
| SCRUM-176 (TAG-009) | 피드 카드 태그 칩 표시 | `PostCard.tsx`에서 `tags.slice(0,3)` 이미 렌더링 중 |
| SCRUM-158 (FEED-018) | 피드 무한 스크롤 | `FeedGrid.tsx`에서 `IntersectionObserver` 이미 구현 |

---

### 🌿 Branch 1: `feat/comments` — 댓글 시스템 풀스택 (진행 중)
**PR:** `[POST/feat] 댓글 시스템 풀스택 구현 (Entity→API→UI)` | **예상:** 약 9시간

#### 백엔드 (6h)
| 이슈 | 설명 | 예상 |
|---|---|---|
| **SCRUM-149** | comments 테이블 Entity 및 Repository 구현 | 1h |
| **SCRUM-150** | 댓글 목록 조회 API (`GET /posts/{postId}/comments`) | 2h |
| **SCRUM-151** | 댓글 작성 API (`POST /posts/{postId}/comments`, 대댓글 `parent_comment_id`) | 2h |
| **SCRUM-152** | 댓글 삭제 API (`DELETE /comments/{commentId}`, 소프트 딜리트) | 1h |

#### 프론트엔드 (3h)
| 이슈 | 설명 | 예상 |
|---|---|---|
| **SCRUM-162** | 댓글 목록/작성/대댓글 UI 구현 및 API 연동 | 3h |

#### 핵심 산출물
- `Comment.java` (Entity, `parent_comment_id`, `depth` 1~2) + `CommentJpaRepository` + `CommentPersistenceAdapter`
- `PostController` → `GET/POST /posts/{postId}/comments`, `DELETE /comments/{commentId}`
- `app/(main)/post/[id]/_components/CommentSection/` (댓글 리스트 + 입력 폼 + 대댓글 표시)
- `feedAPI.ts` → `getComments`, `createComment`, `deleteComment` 클라이언트 함수

#### 핵심 데이터 모델 및 정책
- **데이터 모델**: `comments: comment_id(PK), post_id(FK), user_id(FK), parent_comment_id(FK,nullable), depth(1~2), content(max200)`
- **삭제 정책**: 부모 댓글 삭제 시 대댓글이 남아있다면 "삭제된 댓글입니다" 문구 노출 (데이터는 `deleted_at` 처리)
- **알림 연동**: 댓글/대댓글 생성 시 게시물 작성자에게 `TRYON_COMPLETE`와 유사한 방식으로 알림 생성 로직 추가

---

### 🌿 Branch 2: `feat/reports` — 신고/블라인드 및 비로그인 제어 (✅ 완료됨)
**상태:** 2026-05-12 구현 완료 (통합 테스트 및 프론트엔드 연동 완료)

#### 주요 구현 사항
1. **단계적 신고 제재 시스템 (Graduated Response System)**
   - `1~4회`: 정상 노출 (아무 조치 없음)
   - `5~9회`: **경고 라벨 표시** (`PostDetailResponse`에 `isWarned` 속성 부여, 피드 상세에서 ⚠️ 붉은 배너 노출)
   - `10회`: **자동 블라인드** 및 게시물 작성자에게 **알림 발송** (`REPORT_BLIND` Notification)
   - **유지보수 전략 (임계값 수정 방안):** `Post.java` 도메인 모델 상단에 `REPORT_WARNING_THRESHOLD`, `REPORT_BLIND_THRESHOLD` 상수로 선언되어 있어, 향후 정책 변경 시 **단 한 줄의 숫자 변경**만으로 전체 로직(경고 구간, 블라인드 구간)이 동기화되도록 설계됨.

2. **비로그인 로그인 유도 (Login Gating)**
   - 에러 토스트 대신 모바일 친화적인 `LoginRequiredBottomSheet` 적용 (좋아요, 댓글, 신고 동작 시 자연스럽게 `/login` 유도).

3. **UI/UX 고도화**
   - **신고 모달**: 단순 텍스트 입력창에서 **5가지 카테고리(라디오 버튼)** 선택형으로 개편.
   - **신고 버튼 위치**: 기존 하단 배치에서 우측 상단 **케밥 메뉴(⋮)** 내부로 편입하여 화면 복잡도 완화.

4. **단위 테스트 및 안전성 보장**
   - `ReportServiceTest.java` 작성 완료.
   - 중복 신고 차단, 10회 도달 시 블라인드 트리거, 작성자 자동 알림 발송까지 100% 코드 검증(Coverage) 통과.

---

### 🌿 Branch 3: `feat/search` — 통합 검색 및 태그 자동완성
**PR:** `[TAG/feat] 통합 검색 및 태그 자동완성 풀스택 구현` | **예상:** 약 9시간

#### 백엔드 (4h)
| 이슈 | 설명 | 예상 |
|---|---|---|
| **SCRUM-172** | 태그 자동완성 검색 API (`GET /tags/search?q=`, `usage_count` 내림차순) | 2h |
| **SCRUM-173** | 통합 검색 API (`GET /search?type=tag/caption/all`, 페이지네이션) | 2h |

#### 프론트엔드 (2h)
| 이슈 | 설명 | 예상 |
|---|---|---|
| **SCRUM-175** | 통합 검색 페이지 구현 (`app/(main)/search/page.tsx`) | 2h |

#### 테스트/연동 (3h)
| 이슈 | 설명 | 예상 |
|---|---|---|
| **SCRUM-178** | 태그/검색 API 클라이언트 연동 | 1h |
| **SCRUM-179** | 태그/검색 단위 테스트 및 UI 테스트 | 2h |

---

### 🌿 Branch 4: `feat/post-ux` — 게시물 작성 UX 및 AI 시착 연동
**PR:** `[POST/feat] 게시물 작성 UX 고도화 및 시착 연동` | **예상:** 약 7시간

#### 프론트엔드 (7h)
| 이슈 | 설명 | 예상 |
|---|---|---|
| **SCRUM-142** | Vision API 분석 결과 태그 추천 UI 구현 (칩 형태 토글) | 2h |
| **SCRUM-174** | 게시물 작성 태그 자동완성 UI 구현 (입력 시 드롭다운) | 2h |
| **SCRUM-146** | 시착 직후 게시물 생성 연동 로직 구현 (시착 결과 → 게시물 작성 이미지 자동 완성) | 3h |

---

## 7. 로컬 개발 및 보안 가이드 (Google Cloud Credentials)

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

## 8. 유지보수 가이드

- **태그 사전 추가:** 특정 스타일(Y2K, Gorpcore 등)이나 브랜드를 분석 사전에 추가하고 싶다면, `GoogleVisionTagAdapter.java` 내 `FASHION_WHITELIST` 배열에 단어만 추가하면 즉시 반영됩니다.
- **성능 최적화 (Caching):** 트래픽이 증가할 경우, 동일 이미지에 대한 중복 Vision API 과금 방지를 위해 이미지 해시값 기반 Redis 캐싱 도입을 고려할 수 있습니다.

---

## 9. 기능 검증 및 배포 프로세스 (Testing & Deployment Standard)

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
