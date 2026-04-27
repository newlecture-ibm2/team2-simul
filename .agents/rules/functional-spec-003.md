---
trigger: always_on
---

# Simul 기능 명세 규칙 — Part 3: 계정·프로필·데이터 모델·에러 코드

> 원본: `docs/simul-functional-spec.md`

## 계정 & 프로필 핵심 규칙

### FN-501 소셜 로그인
- 카카오 / 네이버 / 구글 OAuth2
- `POST /auth/social` → JWT Access+Refresh Token 발급
- 신규 사용자: 자동 회원가입 + 크레딧 5회 초기화
- 기존 사용자: 토큰 재발급

### 프로필 (SCR-050)
- ProfileHeader: 이미지+닉네임+한줄소개
- StatsRow: 팔로워/팔로잉/게시물 수
- PostGrid: 해당 유저의 게시물 그리드
- 본인→"프로필 편집", 타인→"팔로우/언팔로우"

### 팔로우
- `POST /follows/{userId}` — 팔로우
- `DELETE /follows/{userId}` — 언팔로우
- UNIQUE(follower_id, following_id)

### 관리자 (Admin)
- `GET /admin/reports` — 신고 목록
- `PATCH /admin/posts/{postId}/blind` — 블라인드
- `PATCH /admin/users/{userId}/suspend` — 유저 정지
- `POST /admin/users/{userId}/credits` — 크레딧 수동 지급

---

## 데이터 모델 (핵심 테이블 요약)

### users
`user_id(PK)`, provider, provider_id, email, nickname, profile_image_url, bio, role(USER/ADMIN), is_active, created_at, updated_at, deleted_at

### follows
`follow_id(PK)`, follower_id(FK→users), following_id(FK→users), created_at, UNIQUE(follower+following)

### base_images
`base_image_id(PK)`, user_id(FK), image_url, source_type(upload/tryon_result), source_post_id(nullable), created_at

### tryon_credits
`credit_id(PK)`, user_id(FK→users), remaining(DEFAULT 5), reset_at

### posts
`post_id(PK)`, user_id(FK), image_url(대표), caption, is_public(DEFAULT false), like_count, comment_count, is_blinded, created_at, updated_at, deleted_at

### post_images
`post_image_id(PK)`, post_id(FK), image_url, sort_order(DEFAULT 0), created_at

### tags
`tag_id(PK)`, name(UNIQUE, max20), category(nullable), usage_count(DEFAULT 0), created_at

### post_tags
post_tag_id(PK), post_id(FK), tag_id(FK), created_at, UNIQUE(post_id+tag_id)

### comments
`comment_id(PK)`, post_id(FK), user_id(FK), parent_comment_id(nullable), content(max200), depth(1~2), created_at, deleted_at

### likes
`like_id(PK)`, post_id(FK), user_id(FK), created_at, UNIQUE(post_id+user_id)

### reports
`report_id(PK)`, post_id(FK), reporter_id(FK), reason, created_at, UNIQUE(post_id+reporter_id)

### closet_items
`item_id(PK)`, user_id(FK), image_id(FK→clothing_images), category(ENUM nullable), memo(max100), try_count(DEFAULT 0), created_at, deleted_at

### clothing_images
`image_id(PK)`, image_url, uploader_id(FK→users), created_at

### notifications
`notification_id(PK)`, recipient_id(FK), actor_id(FK nullable), type(ENUM: TRYON_COMPLETE/LIKE/COMMENT/FOLLOW_POST), reference_id(nullable), message(max200), is_read(DEFAULT false), created_at

---

## 에러 코드 체계

| 코드 | HTTP | 원인 | 사용자 메시지 |
|------|------|------|-------------|
| ERR-000 | 500 | 알 수 없는 서버 오류 | 일시적인 오류 |
| ERR-001 | 401 | 인증 토큰 없음/만료 | 로그인 화면 이동 |
| ERR-002 | 403 | 권한 없음 | 접근 권한 없음 |
| ERR-003 | 404 | 리소스 없음 | 찾을 수 없는 콘텐츠 |
| ERR-101 | 500 | 사진 업로드 실패 | 재시도 |
| ERR-103-A | 422 | 크레딧 소진 | 오늘 무료 시착 모두 사용 |
| ERR-103-B | 500 | AI 생성 실패 | 크레딧 미차감 |
| ERR-103-C | 408 | AI 타임아웃 | 재시도 |
| ERR-103-D | 422 | 부적절 이미지 | 사용 불가 이미지 |
| ERR-201-A | 422 | 옷장 상한 초과 | 옷장 가득 참 (200개) |
| ERR-201-B | 422 | 이미지 크기 초과 | 10MB 이하 |
| ERR-301-A | 422 | 이미지 미첨부 | 업로드 버튼 비활성화 |
| ERR-301-B | 422 | 게시물 이미지 초과 | 20MB 이하 |
| ERR-301-D | 422 | 이미지 5장 초과 | 최대 5장 |
| ERR-304-A | 401 | 비로그인 좋아요 | 로그인 유도 |
| ERR-305-A | 401 | 비로그인 댓글 | 로그인 유도 |
| ERR-305-B | 422 | 댓글 200자 초과 | 카운터 빨간색 |
| ERR-307-A | 422 | 태그 10개 초과 | 추가 비활성화 |
| ERR-307-B | 500 | Vision API 실패 | 수동 입력 안내 |
| ERR-401-A | 422 | 중복 신고 | 이미 신고함 |

## 공통 예외 처리 원칙
1. **낙관적 업데이트**: 좋아요 등 UI 먼저→실패 시 롤백
2. **비로그인 접근**: 피드/상세 열람 허용, 시착/저장/좋아요/댓글은 로그인 유도
3. **네트워크 없음**: 전역 오프라인 배너 + 캐시 데이터 읽기 전용
4. **토큰 만료**: 자동 갱신→실패 시 로그인 이동
5. **서버 점검**: 503 → 점검 안내 화면
