---
trigger: always_on
---

# Simul 명명 규칙 및 핵심 정책

> 원본: `docs/simul-conventions.md`

## 명명 규칙

### DB / SQL
- **snake_case**, 테이블명 **복수형** (예: `users`, `posts`, `notifications`)
- PK는 **UUID**, FK는 `참조테이블_단수_id` (예: `user_id`, `post_id`)

### Backend (Java 21 / Spring Boot 3.3)
- 변수/메서드: **camelCase** | 클래스/인터페이스: **PascalCase**
- 패키지: **소문자 단일 단어** (`auth`, `user`, `post`, `tag`, `notification`)
- 헥사고날 패키지: `domain/model/`, `application/port/in|out/`, `application/service/`, `adapter/in/web/`, `adapter/out/persistence/`

### Frontend (TS 5.5 / Next.js 15 / React 19)
- 함수/변수: **camelCase** | 컴포넌트/타입: **PascalCase**
- 파일/폴더/URL: **kebab-case** | 훅: `useCamelCase` | 스토어: `usePascalCaseStore`

### REST API URL
- `/api/...` 경로, 버전 세그먼트 미사용, **kebab-case 복수형**
- 예: `GET /api/posts`, `POST /api/posts/{postId}/likes`, `GET /api/notifications`

## BFF (Next.js 15)
- 브라우저는 `/api/...`만 호출, Middleware가 백엔드로 프록시
- JWT는 **httpOnly Cookie** 기반, Axios 인터셉터 자동 갱신

## 핵심 정책

### Soft Delete
- 모든 엔티티는 **BaseEntity** 상속 (`created_at`, `updated_at`, `deleted_at`)
- **DELETE 쿼리 물리 삭제 금지**, `softDelete()` 호출 + `save()` 명시
- 조회 시 `WHERE deleted_at IS NULL` 기본 필터

### UUID PK
- 모든 PK는 **UUID v4**, `@GeneratedValue(strategy = GenerationType.UUID)`

### 이미지 처리
- MVP: **서버 로컬 디스크** 저장 (`/uploads/images/`), 향후 S3 전환 추상화
- 포맷(JPG/PNG/WebP), 용량(최대 20MB), 해상도 서버 검증 필수
- URL은 상대 경로 저장 (예: `/uploads/images/2026/04/uuid.jpg`)

### 크레딧
- 일일 5회, KST 자정 리셋, **성공 시에만** 차감

## 역할 (USER / ADMIN)
- DB·JWT에 접두사 없이 저장 (`USER`, `ADMIN`)
- Spring Security: `hasRole("ADMIN")` (ROLE_ 없이)
- Guest(비로그인): 피드/상세 읽기 전용, 인증 액션 시 로그인 유도 바텀시트

## 도메인 의존성 (순환 금지)
```
Auth → User | Closet → User | Post → User, Tag
TryOn → Closet, Post | Admin → User, Post, TryOn
Tag → (없음) | Notification ← TryOn, Post
```
- Port(UseCase) 인터페이스로만 타 도메인 접근. Repository/Entity 직접 접근 금지.

## 에러 처리
- 공통 형식: `{ "error_code", "message", "detail" }`
- 코드 접두사: `ERR-0xx`(공통) `ERR-1xx`(시착) `ERR-2xx`(옷장) `ERR-3xx`(피드/태그) `ERR-4xx`(신고)
- 낙관적 업데이트 → 실패 시 롤백 | 토큰 만료 → 자동 갱신 → 실패 시 로그인 이동
