---
trigger: always_on
---

# Simul API 엔드포인트 규칙

> 원본: `docs/simul-api-spec.md`

## 공통
- Header: `Authorization: Bearer {jwt}`, `Content-Type: application/json`
- Error: `{ "error_code", "message", "detail" }`

## 인증/사용자
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/auth/social` | 소셜 로그인 (kakao/naver/google) → JWT 발급 |
| POST | `/auth/refresh` | 토큰 갱신 |
| DELETE | `/auth/logout` | 로그아웃 |
| GET | `/users/me` | 내 정보 조회 |
| PATCH | `/users/me` | 프로필 수정 (nickname, bio, profile_image_url) |
| DELETE | `/users/me` | 회원 탈퇴 (소프트 딜리트) |
| GET | `/users/{userId}` | 사용자 프로필 (팔로워/팔로잉 수 포함) |
| GET | `/users/{userId}/posts` | 사용자 게시물 목록 (본인: 공개+비공개, 타인: 공개만) |
| POST | `/follows/{userId}` | 팔로우 |
| DELETE | `/follows/{userId}` | 언팔로우 |

## AI 가상시착
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/tryon/base-images` | 베이스 이미지 업로드 (multipart, JPG/PNG/HEIC, max20MB) |
| POST | `/tryon/base-images/from-post` | 시착 결과→베이스 복제 등록 |
| GET | `/users/me/base-images` | 내 베이스 이미지 목록 |
| POST | `/tryon/generate` | AI 시착 생성 요청 → job_id 반환 |
| GET | `/tryon/status/{jobId}` | SSE 스트림 (processing→completed) |
| GET | `/tryon/credits` | 크레딧 잔여 조회 (remaining/5) |

## 게시물
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/posts` | 게시물 작성 (이미지 1~5장 + 태그 최대10 + 캡션 300자) |
| GET | `/posts` | 피드 조회 (tab=all/following, sort=recent/popular, 페이지네이션) |
| GET | `/posts/{postId}` | 게시물 상세 (is_liked 포함) |
| DELETE | `/posts/{postId}` | 게시물 삭제 (소프트 딜리트) |
| POST | `/posts/{postId}/likes` | 좋아요 토글 |
| GET | `/posts/{postId}/comments` | 댓글 목록 (대댓글 포함) |
| POST | `/posts/{postId}/comments` | 댓글 작성 (parent_comment_id로 대댓글) |
| DELETE | `/comments/{commentId}` | 댓글 삭제 |
| POST | `/posts/{postId}/report` | 게시물 신고 (중복 방지, 5회→자동 블라인드) |

## 옷장
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/closet/items` | 아이템 추가 (max200개, image10MB) |
| GET | `/closet/items` | 목록 조회 (category, sort=recent/most_tried, 페이지네이션) |
| GET | `/closet/items/{itemId}` | 아이템 상세 |
| PATCH | `/closet/items/{itemId}` | 아이템 수정 (category, memo) |
| DELETE | `/closet/items/{itemId}` | 아이템 삭제 (소프트 딜리트) |

## 태그/검색
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/tags/analyze` | Vision API 이미지 태그 분석 (패션 태그 confidence≥0.5, 색상 태그 confidence≥0.8, 한국어 자동 번역) |
| GET | `/tags/search` | 태그 자동완성 (usage_count 내림차순) |
| GET | `/search` | 통합 검색 (type=tag/caption/all, 페이지네이션) |

## 알림
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/notifications` | 알림 목록 (unread 우선) |
| GET | `/notifications/unread-count` | 미읽음 수 |
| PATCH | `/notifications/{id}/read` | 개별 읽음 |
| PATCH | `/notifications/read-all` | 전체 읽음 |

## 관리자
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/admin/reports` | 신고 목록 |
| PATCH | `/admin/posts/{postId}/blind` | 게시물 블라인드 |
| PATCH | `/admin/posts/{postId}/unblind` | 블라인드 해제 |
| PATCH | `/admin/users/{userId}/suspend` | 유저 정지 |
| POST | `/admin/users/{userId}/credits` | 크레딧 수동 지급 |
