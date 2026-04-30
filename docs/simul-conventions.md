# 📜 [Simul] 명명 규칙 및 핵심 정책 (General Conventions & Workflow)

이 파일은 AI 에이전트와 우리 팀이 자율적으로 따를 **프로젝트 공통 코딩 스탠다드**입니다.  
프롬프트 요청 없이도 이 규칙은 시스템 전체에 1순위로 강제 반영됩니다.

> **기준 문서**: `simul-mvp-spec.md`, `simul-backend-architecture.md`, `simul-api-spec.md`, `simul-erd.md`, `simul-functional-spec.md`

---

## 1. Naming Conventions (명명 규칙)

### 1-1. Database / SQL Schema

- 철저한 **`snake_case`** 사용.
- 물리 테이블명은 **복수형 `snake_case`** (예: `users`, `posts`, `tags`, `notifications`).
- JPA `@Table(name = "users")` 등 문서·스키마·`simul-erd.md` 기준과 반드시 맞춘다.
- PK는 **UUID** 타입을 사용한다 (예: `user_id`, `post_id`, `notification_id`).
- FK 컬럼명은 **참조 테이블 단수형 + `_id`** 형식을 따른다 (예: `user_id`, `post_id`, `tag_id`).

### 1-2. Backend (Java 21 / Spring Boot 3.3)

- 변수, 메서드, 파라미터는 **`camelCase`** 사용.
- 클래스, 인터페이스는 **`PascalCase`** 사용.
- 패키지는 **소문자 단일 단어** (예: `auth`, `user`, `post`, `tag`, `notification`).
- 도메인별 패키지 구조는 헥사고날 아키텍처를 따른다:
  ```
  {domain}/
    ├── domain/model/         # Entity, VO
    ├── application/
    │   ├── port/in/          # UseCase 인터페이스
    │   ├── port/out/         # Persistence/External Port 인터페이스
    │   ├── service/          # UseCase 구현체
    │   └── dto/              # Command, Response DTO
    └── adapter/
        ├── in/web/           # Controller + Request/Response DTO
        └── out/persistence/  # JPA Adapter 구현체
  ```

### 1-3. Frontend (TypeScript 5.5 / Next.js 15 / React 19)

- 일반 함수 및 변수는 **`camelCase`**.
- React **컴포넌트 및 Type/Interface**는 **`PascalCase`** 사용.
- 파일 및 폴더 이름 (URL 경로 포함)은 **`kebab-case`** 사용 (예: `/my-closet`, `post-detail`).
- 훅(Hook)은 `use` 접두사 + **`camelCase`** (예: `useNotifications`, `useTryonStatus`).
- 상태 스토어는 `use` + **`PascalCase`** + `Store` (예: `useAuthStore`, `useNotificationStore`).

### 1-4. REST API 엔드포인트 URL (백엔드 계약)

- Spring에 매핑되는 공개 API는 **`/api/...`** 경로를 사용한다. (문서·OpenAPI·`simul-api-spec.md` 기준).
- **`/api/v1`** 등 버전 세그먼트는 사용하지 않는다.
- 소문자 **`kebab-case`**, 리소스 세그먼트는 **복수형(Plural)** (예: `/api/posts`, `/api/notifications`, `/api/tags`).
- 경로 변수는 **`camelCase`** (예: `/api/posts/{postId}`, `/api/notifications/{notificationId}`).
- 예시:
  ```
  GET    /api/posts                    ← 피드 목록
  POST   /api/posts                    ← 게시물 작성
  GET    /api/posts/{postId}           ← 게시물 상세
  POST   /api/posts/{postId}/likes     ← 좋아요 토글
  GET    /api/notifications            ← 알림 목록
  PATCH  /api/notifications/read-all   ← 전체 읽음 처리
  POST   /api/tryon/generate           ← 시착 생성 요청
  GET    /api/tryon/status/{jobId}     ← SSE 스트림
  ```

---

## 2. BFF (Next.js 15 App Router) — 프론트 호출면

- 브라우저·클라이언트 컴포넌트는 **`/api/...`** 만 호출한다. 백엔드 호스트는 서버 전용 환경변수(`BACKEND_URL`)로만 관리한다.
- **프록시 경로**는 백엔드와 **동일한 `/api/` 이하 세그먼트**와 1:1 대응하며, Next.js Middleware 또는 API Route(`route.ts`)가 이를 가로채 백엔드로 포워딩한다.
  ```
  브라우저 GET /api/posts → Next.js Middleware → Spring GET /api/posts
  ```
- 프론트엔드 코드 내에 수동으로 `/bff`, `/proxy` 등 세그먼트를 추가하지 않는다.
- JWT 토큰은 **httpOnly Cookie** 기반으로 관리하며, Axios 인터셉터에서 자동 갱신 로직을 처리한다.

---

## 3. Core Policies (핵심 데이터 규칙)

### 3-1. Soft Delete 지침 (BaseEntity 활용 강제)

- 시스템 내 모든 JPA 엔티티는 반드시 **`BaseEntity`** (`com.simul.common.entity.BaseEntity`)를 상속(`extends`)받아 공통 필드를 상속받아야 한다:
  ```java
  @MappedSuperclass
  public abstract class BaseEntity {
      @Column(name = "created_at", nullable = false, updatable = false)
      private LocalDateTime createdAt;

      @Column(name = "updated_at")
      private LocalDateTime updatedAt;

      @Column(name = "deleted_at")
      private LocalDateTime deletedAt;

      public void softDelete() {
          this.deletedAt = LocalDateTime.now();
      }

      public boolean isDeleted() {
          return this.deletedAt != null;
      }
  }
  ```
- 데이터베이스의 어떤 테이블에서도 **`DELETE` 쿼리 사용(물리 삭제)을 엄격히 금지**한다.
- 데이터 삭제 시에는 엔티티 객체에서 **`softDelete()`** 메서드를 호출해 삭제 시각을 기록하고, 어댑터 계층에서 반드시 **`jpaRepository.save(entity)`를 명시적으로 호출**한다 (더티 체킹에 의존하지 않는다).
- **조회 시 기본 필터**: `WHERE deleted_at IS NULL` 조건을 기본 적용한다 (`@Where` 어노테이션 또는 QueryDSL/JPA Specification 활용).

### 3-2. UUID 기본 키 정책

- 모든 테이블의 PK는 **UUID v4**를 사용한다.
- JPA에서 `@GeneratedValue(strategy = GenerationType.UUID)` 또는 `UUID.randomUUID()`로 생성한다.
- 외부 노출 시 UUID 문자열 형태 (예: `"550e8400-e29b-41d4-a716-446655440000"`).

### 3-3. 이미지 처리 정책

- MVP 단계에서 이미지는 **자체 서버 로컬 디스크**에 저장한다 (`/uploads/images/` 경로).
- 트래픽이 증가하면 S3 + CDN으로 전환한다 (저장 경로만 변경하면 되도록 추상화).
- 업로드 시 **포맷(JPG/PNG/WebP), 용량(최대 20MB), 해상도** 서버 검증을 반드시 수행한다.
- 이미지 URL은 상대 경로로 저장한다 (예: `/uploads/images/2026/04/uuid.jpg`).
- 프론트엔드는 **`next/image`** 컴포넌트를 사용하여 자동 Lazy Load, 리사이징, WebP 변환을 적용한다.

### 3-4. 크레딧 시스템 정책

- AI 시착 크레딧은 **일일 5회 제한**, KST 자정(`Asia/Seoul`) 기준 자동 리셋.
- 시착 **성공 시에만** 크레딧을 차감한다 (실패/타임아웃 시 차감하지 않음).
- 관리자가 수동으로 크레딧을 지급할 수 있다 (`POST /api/admin/users/{userId}/credits`).

---

## 4. Role & Permission (역할 통제)

Simul은 **2가지 역할(USER, ADMIN)** 을 사용한다.

| 구분 | 표기 | 예시 |
|------|------|------|
| **DB `users.role` 컬럼 · JWT payload `role`** | 접두사 **없음** | `USER`, `ADMIN` |
| **Spring Security 코드** | `hasRole("…")` — 인자에는 **`ROLE_` 없이** 역할명만 | `hasRole("ADMIN")` |
| **기획·명세·다이어그램** | 가독성을 위해 `ROLE_` 접두를 쓸 수 있음 | `ROLE_USER` — **저장 값이 아님** |

**접근 권한 정책:**

| 역할 | 허용 범위 |
|------|----------|
| **Guest (비로그인)** | 피드/게시물 상세 열람, 태그 검색 (읽기 전용) |
| **USER** | 위 + 시착 생성, 게시물 작성, 좋아요/댓글, 옷장 CRUD, 팔로우, 알림, 프로필 편집 |
| **ADMIN** | `/api/admin/**` — 게시물 블라인드, 유저 정지, 크레딧 수동 지급, 신고 목록 조회 |

- 비로그인 사용자가 인증 필요 액션(시착/좋아요/댓글 등) 시도 시: **로그인 유도 바텀시트** 표시.
- 백엔드: Spring Security `@PreAuthorize` 어노테이션으로 인가 처리.
- 프론트엔드: 인증 상태에 따른 UI 분기 (Zustand `useAuthStore` 활용).

---

## 5. 도메인 간 의존성 원칙

### 5-1. 의존 방향 규칙 (순환 금지)

```text
Auth → User
Closet → User
Post → User, Tag
TryOn → Closet, Post
Notification ← TryOn, Post (알림 생성 요청)
Admin → User, Post, TryOn
Tag → (없음 — 다른 도메인에 의존하지 않음)
```

- **순환 의존은 절대 금지**한다. `A → B`이면 `B → A` 금지.
- 다른 도메인 데이터가 필요하면, **해당 도메인이 제공하는 Input Port(UseCase) 인터페이스를 DI 주입받아 호출**한다.
- 다른 도메인의 Repository, Entity, Adapter에 직접 접근하지 않는다.

### 5-2. 통합 검색 오케스트레이션

- `GET /api/search`는 **SearchController**가 Post 도메인과 Tag 도메인을 **동시에 호출**하여 결과를 병합한다.
- SearchController는 자체 비즈니스 로직을 가지지 않으며, **오케스트레이션만 수행**한다.
- Post → Tag 단방향 의존 유지. Tag는 Post를 의존하지 않는다.

---

## 6. Git & 코드 리뷰 정책

### 6-1. 브랜치 전략

| 브랜치 | 용도 |
|--------|------|
| `main` | 배포 대상 — 직접 커밋 금지 |
| `develop` | 개발 통합 브랜치 |
| `feature/{도메인}-{기능}` | 기능 개발 (예: `feature/post-create`, `feature/noti-tryon-trigger`) |
| `fix/{이슈번호}-{설명}` | 버그 수정 |

### 6-2. 커밋 메시지 규칙

```
{type}: {한글 또는 영문 설명}

type: feat | fix | docs | style | refactor | test | chore
```

예시:
```
feat: 게시물 작성 API 구현 (POST /api/posts)
fix: 좋아요 토글 시 like_count 동기화 오류 수정
docs: 알림 기능 ERD 및 API 명세 추가
test: 시착 도메인 단위 테스트 추가
```

---

## 7. 공통 응답 및 에러 처리 표준

### 7-1. 공통 무한 스크롤(커서 기반) Response 형식

데이터의 중복/누락을 방지하고 성능을 최적화하기 위해, 피드 및 목록 API는 커서(Cursor) 기반 무한 스크롤을 권장하며 `com.simul.common.dto.CursorResponse<T>`를 사용하여 아래 규격으로 응답한다:
```json
{
  "data": [...],
  "next_cursor": "550e8400-e29b-41d4-a716-446655440000",
  "has_next": true
}
```
- 다음 페이지가 없을 경우 `next_cursor`는 `null`이며 `has_next`는 `false`로 내려간다.
- 기존의 오프셋 방식 무한 스크롤이 불가피한 경우 `PageResponse<T>`를 혼용할 수 있다.

### 7-2. 에러 Response 형식

모든 에러는 공통 형식을 따른다:
```json
{
  "error_code": "ERR-301-A",
  "message": "사용자에게 노출되는 메시지",
  "detail": "개발자용 상세 메시지"
}
```

### 7-3. 에러 코드 체계

에러 코드는 `simul-functional-spec.md` §10에 정의된 코드를 따른다. 새 에러 추가 시 반드시 문서를 먼저 업데이트한다.

| 접두사 | 도메인 |
|--------|--------|
| `ERR-0xx` | 공통 (인증, 권한, 서버) |
| `ERR-1xx` | AI 시착 |
| `ERR-2xx` | 개인 옷장 |
| `ERR-3xx` | 커뮤니티 피드 / 태그 |
| `ERR-4xx` | 신고 |

### 7-4. 프론트엔드 에러 처리

- **낙관적 업데이트**: 좋아요 등 빠른 인터랙션은 UI 먼저 반영 → API 실패 시 롤백.
- **네트워크 없음**: 전역 오프라인 배너 표시.
- **토큰 만료**: Axios 인터셉터에서 자동 토큰 갱신 시도 → 실패 시 로그인 화면 이동.
