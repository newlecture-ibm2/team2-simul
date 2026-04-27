---
trigger: always_on
---

# Simul 기능 명세 규칙 — Part 2: 커뮤니티 피드·태그·검색·알림

> 원본: `docs/simul-functional-spec.md`

## 커뮤니티 피드 핵심 규칙

### FN-301 게시물 작성
- 이미지: 최소 1장, 최대 **5장** (로컬 앨범 + 서버 저장 시착 결과 혼합 가능)
- 시착 직후(SCR-024→SCR-012) 진입 시 시착 결과 이미지 **첫 번째 자동 완성**
- 태그: Google Vision API 자동 추천 → 사용자 편집 (최대 **10개**)
- 캡션: 최대 300자
- 공개/비공개 토글
- 이미지 순서 드래그 앤 드롭 변경 가능

### FN-302 피드 조회
- **전체/팔로잉** 2탭
- **최신순/인기순** 정렬 토글
- 무한 스크롤 (Intersection Observer) + 이미지 Lazy Load
- 2열 반응형 그리드
- PostCard: 썸네일 + 태그 칩 최대 3개

### FN-304 좋아요 [Should]
- 낙관적 업데이트: UI 먼저 반영→API 실패 시 롤백
- `POST /posts/{postId}/likes` 토글 (like/unlike)
- UNIQUE(post_id, user_id) — 중복 방지
- 비로그인 시 로그인 유도 바텀시트 (ERR-304-A)

### FN-305 댓글 [Could]
- 최대 2 Depth (대댓글)
- 200자 제한 (ERR-305-B)
- `parent_comment_id`로 대댓글 구현
- 비로그인 시 로그인 유도 (ERR-305-A)

### 신고 [Should]
- `POST /posts/{postId}/report` — 사유 입력
- UNIQUE(post_id, reporter_id) — 중복 신고 방지 (ERR-401-A)
- **5회 누적 시 자동 블라인드** (is_blinded=true)

---

## 태그 시스템 핵심 규칙

### FN-307 자동 태그 (Google Vision API)
1. 이미지 업로드/선택 시 Vision API Label Detection 호출
2. 옷 관련 키워드만 필터링 (confidence ≥ 0.7)
3. 추천 태그를 ChipGroup으로 노출
4. 사용자가 +/- 편집 (최대 10개, ERR-307-A)
5. 확정 태그를 `tags` + `post_tags` 테이블에 upsert

### Vision API 실패 시 (ERR-307-B)
- "자동 태그 추출에 실패했어요. 수동으로 입력해주세요"

### 태그 저장 구조
- `tags` 테이블: tag_id, name(UNIQUE), category, usage_count
- `post_tags` 테이블: post_id + tag_id (N:M 매핑)
- usage_count로 인기 태그 정렬

---

## 통합 검색 핵심 규칙

### FN-308 통합 검색 (SCR-070)
- 검색바 `#` 입력 시 **태그 모드** 전환 + 자동완성 드롭다운
- 검색 유형: tag / caption / all
- 검색 결과: 게시물 그리드 + 연관 태그
- 검색어 미입력 시 **인기 태그** 칩 기본 노출
- 최근 검색어 로컬 저장

---

## 알림 핵심 규칙

### FN-601 알림 (SCR-080) [Must]

**알림 유형 4가지:**
| 유형 | 트리거 | 수신자 | 탭 시 이동 |
|------|--------|--------|-----------|
| `TRYON_COMPLETE` | AI 시착 완료 | 시착 요청자 | SCR-024 |
| `LIKE` | 내 게시물에 좋아요 | 게시물 작성자 | SCR-011 |
| `COMMENT` | 내 게시물에 댓글 | 게시물 작성자 | SCR-011 |
| `FOLLOW_POST` | 팔로우한 사람 새 게시물 | 팔로워 | SCR-011 |

**규칙:**
- 본인 활동(좋아요/댓글)에 대한 알림 **생성 안 함**
- 헤더에 미읽음 배지 표시
- 전체 읽음 버튼
- 보관 기간: **30일** (이후 자동 삭제)
- 페이지네이션 지원

### 알림 API
- `GET /notifications` — 목록 조회 (unread 우선)
- `GET /notifications/unread-count` — 미읽음 수 (배지)
- `PATCH /notifications/{id}/read` — 개별 읽음
- `PATCH /notifications/read-all` — 전체 읽음
