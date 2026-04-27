# Simul API 명세서 (API Specification) v1.0

> **문서 목적**: Simul 클라이언트와 백엔드 서버 간의 통신 규격을 정의합니다.  
> **기준 사항**: `simul-functional-spec.md` (기능 명세서)

---

## 📌 공통 규격

### 공통 Request Header
```http
Authorization: Bearer {jwt_access_token}
Content-Type: application/json
Accept-Language: ko
```

### 공통 Error Response 형식
```json
// HTTP Status: 4xx, 5xx
{
  "error_code": "ERR-101",
  "message": "사용자에게 노출되는 메시지",
  "detail": "에러 원인 상세 (디버그 용도)"
}
```

---

## 1. 인증 및 사용자 (Auth & Users)

### [POST] `/auth/social` - 소셜 로그인
**Request**
```json
{
  "provider": "kakao",  // kakao | naver | google
  "access_token": "oauth_provider_access_token"
}
```
**Response `200`**
```json
{
  "access_token": "jwt_access_token",
  "refresh_token": "jwt_refresh_token",
  "user": {
    "user_id": "uuid",
    "nickname": "패션러버",
    "profile_image_url": "https://cdn.simul.io/...jpg",
    "is_new_user": false
  }
}
```

### [POST] `/auth/refresh` - 토큰 갱신
**Request**
```json
{
  "refresh_token": "jwt_refresh_token"
}
```
**Response `200`**
```json
{
  "access_token": "new_jwt_access_token",
  "refresh_token": "new_jwt_refresh_token"
}
```

### [DELETE] `/auth/logout` - 로그아웃
**Request**: `(Headers Only)`
**Response `200`** (빈 구조체 반환)

### [GET] `/users/me` - 내 정보 조회
**Response `200`**
```json
{
  "user_id": "uuid",
  "provider": "kakao",
  "nickname": "패션러버",
  "bio": "안녕하세요",
  "profile_image_url": "https://cdn.simul.io/...jpg"
}
```

### [PATCH] `/users/me` - 프로필 수정
**Request**
```json
{
  "nickname": "새닉네임",         // optional
  "bio": "패션을 사랑해요",         // optional, max: 50
  "profile_image_url": "https://..." // optional
}
```
**Response `200`**: 수정된 유저 정보 반환

### [DELETE] `/users/me` - 회원 탈퇴
**Response `200`**: 성공 시 `deleted_at` 적용 (소프트 딜리트)

### [GET] `/users/{user_id}` - 사용자 프로필 조회
**Response `200`**
```json
{
  "user_id": "uuid",
  "nickname": "사용자명",
  "bio": "안녕하세요",
  "profile_image_url": "...",
  "follower_count": 102,
  "following_count": 50,
  "is_following": false // 내가 상대를 팔로우 중인지 여부
}
```

### [GET] `/users/{user_id}/posts` - 특정 사용자 게시물 목록 (내 시착 이력 포함)
**Response `200`**
```json
{
  "posts": [
    {
      "post_id": "uuid",
      "image_url": "...",
      "is_public": true,
      "like_count": 10,
      "created_at": "ISO8601"
    }
  ],
  "total": 15
}
```
*Note: `{user_id}`가 나 자신(me)일 경우 `is_public=false`인 비공개 시착 이력도 모두 포함되어 반환됩니다. 타인 프로필 조회 시에는 `is_public=true`인 공개 게시물만 반환됩니다.*

### [POST] `/follows/{user_id}` - 팔로우
**Response `201 Created`**

### [DELETE] `/follows/{user_id}` - 언팔로우
**Response `200 OK`**

---

## 2. 디지털 옷장 (Closet)

### [POST] `/closet/items` - 아이템 추가
**Request**
```json
{
  "image_url": "https://cdn.simul.io/items/abc.jpg",
  "category": "top",   // optional: top | bottom | outer | shoes | accessory
  "memo": "여름용"      // optional, max: 100
}
```
**Response `201`**
```json
{
  "item_id": "item_uuid",
  "created_at": "ISO8601"
}
```

### [GET] `/closet/items` - 아이템 목록 조회
**Query Params**
- `category` (optional)
- `sort` (recent | most_tried)
- `page=1&per_page=20`

**Response `200`**
```json
{
  "items": [
    {
      "item_id": "item_uuid",
      "image_url": "https://...",
      "category": "top",
      "memo": "여름용",
      "try_count": 3,
      "created_at": "ISO8601"
    }
  ],
  "total": 42,
  "page": 1,
  "per_page": 20
}
```

### [GET] `/closet/items/{item_id}` - 아이템 상세 조회
**Response `200`** (단건 아이템 객체 반환)

### [PATCH] `/closet/items/{item_id}` - 아이템 정보 수정
**Request**
```json
{
  "category": "bottom", // optional
  "memo": "가을용 바지"   // optional
}
```
**Response `200`** (수정된 객체 정보 반환)

### [DELETE] `/closet/items/{item_id}` - 아이템 삭제
**Response `200`**
```json
{
  "item_id": "item_uuid",
  "deleted_at": "ISO8601"
}
```

---

## 3. 커뮤니티 피드 (Feed)

### [POST] `/posts` - 게시물 작성 및 발행 (다중 이미지 + 태그 지원)
**Request**
```json
{
  "image_urls": [
    "https://cdn.simul.io/posts/img1.jpg",
    "https://cdn.simul.io/posts/img2.jpg"
  ],
  "tags": ["데님", "캐주얼", "스트릿"], // optional, 최대 10개
  "caption": "오늘 코디",                 // optional, max: 300
  "is_public": true                     // 기본값 true
}
```
*Note: `image_urls`는 최소 1개, 최대 5개. 로컬 업로드 URL 또는 서버에 저장된 시착 결과 URL을 혼합하여 사용할 수 있습니다. 첫 번째 URL이 대표 이미지(`posts.image_url`)로 저장됩니다. 시착 직후 진입 시 시착 결과 이미지가 첫 번째로 자동 완성됩니다.*
*Note: `tags` 배열의 각 태그는 `tags` 테이블에 upsert 후 `post_tags`로 매핑됩니다.*

**Response `201`**
```json
{
  "post_id": "post_uuid",
  "tags": ["데님", "캐주얼", "스트릿"],
  "created_at": "ISO8601"
}
```

### [GET] `/posts` - 피드 목록 조회
**Query Params**
- `tab=all` (all | following)
- `sort=recent` (recent | popular)
- `tag` (optional, 태그 필터 — 예: `tag=데님`)
- `page=1&per_page=20`

**Response `200`**
```json
{
  "posts": [
    {
      "post_id": "post_uuid",
      "image_url": "https://...",
      "image_count": 3,
      "tags": ["데님", "캐주얼"],
      "like_count": 42,
      "author": {
        "user_id": "user_uuid",
        "nickname": "패션러버",
        "profile_image_url": "https://..."
      },
      "created_at": "ISO8601"
    }
  ],
  "total": 200,
  "page": 1,
  "per_page": 20
}
```

### [GET] `/posts/{post_id}` - 단일 게시물 상세
**Response `200`**
```json
{
  "post_id": "post_uuid",
  "images": [
    { "image_url": "https://...", "sort_order": 0 },
    { "image_url": "https://...", "sort_order": 1 }
  ],
  "tags": [
    { "tag_id": "uuid", "name": "데님", "category": "하의" },
    { "tag_id": "uuid", "name": "캐주얼", "category": "스타일" }
  ],
  "like_count": 42,
  "is_liked": true,
  "author": {
    "user_id": "user_uuid",
    "nickname": "패션러버",
    "profile_image_url": "https://..."
  },
  "caption": "오늘 코디",
  "is_public": true,
  "created_at": "ISO8601"
}
```

### [DELETE] `/posts/{post_id}` - 게시물 삭제
**Response `200 OK`**

### [POST] `/posts/{post_id}/likes` - 좋아요 토글
**Response `200`**
```json
{
  "liked": true,
  "like_count": 43
}
```

### [GET] `/posts/{post_id}/comments` - 댓글 조회
**Response `200`**
```json
{
  "comments": [
    {
      "comment_id": "cmt_uuid",
      "user": { "user_id": "uuid", "nickname": "사용자명" },
      "content": "이쁘네요",
      "depth": 1,
      "parent_comment_id": null,
      "created_at": "ISO8601",
      "replies": [ /* 대댓글 배열 */ ]
    }
  ]
}
```

### [POST] `/posts/{post_id}/comments` - 댓글 작성
**Request**
```json
{
  "content": "감사합니다!",     // max 200자
  "parent_comment_id": null   // 대댓글일 경우 원본 comment_id
}
```
**Response `201 Created`**

### [DELETE] `/comments/{comment_id}` - 댓글 삭제
**Response `200 OK`**

### [POST] `/posts/{post_id}/report` - 게시물 신고
**Request**
```json
{
  "reason": "부적절한 이미지"
}
```
**Response `201 Created`**

---

## 3-1. 태그 및 검색 (Tags & Search)

### [POST] `/tags/analyze` - 이미지 태그 자동 추출 (Google Vision API)
**Request**
```json
{
  "image_url": "https://cdn.simul.io/posts/img1.jpg"
}
```
**Response `200`**
```json
{
  "suggested_tags": [
    { "name": "데님", "category": "하의", "confidence": 0.95 },
    { "name": "캐주얼", "category": "스타일", "confidence": 0.88 },
    { "name": "스트라이프", "category": "소재/패턴", "confidence": 0.72 }
  ]
}
```
*Note: Google Vision API의 라벨 감지 결과를 옷 관련 키워드로 필터링하여 반환합니다. `confidence`는 Vision API의 신뢰도 점수입니다.*

### [GET] `/tags/search` - 태그 자동완성 검색
**Query Params**
- `q` (검색 키워드, 필수)
- `limit=10` (최대 반환 수)

**Response `200`**
```json
{
  "tags": [
    { "tag_id": "uuid", "name": "데님", "category": "하의", "usage_count": 1520 },
    { "tag_id": "uuid", "name": "데님자켓", "category": "아우터", "usage_count": 340 }
  ]
}
```
*Note: `usage_count` 내림차순으로 정렬되어 반환됩니다. 인증 불필요 API입니다.*

### [GET] `/search` - 통합 검색
**Query Params**
- `q` (검색 키워드, 필수)
- `type=all` (all | tag | caption)
- `page=1&per_page=20`

**Response `200`**
```json
{
  "posts": [
    {
      "post_id": "post_uuid",
      "image_url": "https://...",
      "tags": ["데님", "캐주얼"],
      "like_count": 42,
      "author": {
        "user_id": "user_uuid",
        "nickname": "패션러버"
      },
      "created_at": "ISO8601"
    }
  ],
  "related_tags": ["데님자켓", "데님셔츠"],
  "total": 50,
  "page": 1,
  "per_page": 20
}
```
*Note: `related_tags`는 검색 키워드와 연관된 태그를 추가로 반환합니다. 인증 불필요 API입니다.*

---

## 4. AI 가상 시착 (Try-On)

### [GET] `/users/me/base-images` - 내 사람(베이스) 이미지 목록 조회
**Response `200`**
```json
{
  "base_images": [
    {
      "base_image_id": "uuid",
      "image_url": "https://...",
      "created_at": "ISO8601"
    }
  ]
}
```

### [POST] `/tryon/base-images` - 새 사람 이미지 기기 업로드 및 등록
**Request Forms (`multipart/form-data`)**
- `image`: File (JPG, PNG, HEIC / Max: 20MB)

**Response `201`**
```json
{
  "base_image_id": "uuid",
  "image_url": "https://cdn.simul.io/users/photo_abc.jpg"
}
```

### [POST] `/tryon/base-images/from-post` - 이미 AI로 완성된 결과를 다시 사람 이미지로 복제 등록
**Request**
```json
{
  "source_post_id": "post_uuid" // 과거 성공적인 시착 이력(게시물) ID
}
```
**Response `201`**
```json
{
  "base_image_id": "new_uuid",
  "image_url": "https://..."
}
```

### [POST] `/tryon/generate` - AI 시착 생성 요청
**Request**
```json
{
  "base_image_id": "uuid",      // 선택/등록된 내 사람 이미지 ID
  "item_id": "item_uuid"        // 입어볼 옷장 속 아이템 ID
}
```
**Response `201`**
```json
{
  "job_id": "post_uuid", // 비공개 게시물(Post) 통합 모델의 ID로 생성됨
  "status": "processing",
  "estimated_seconds": 20
}
```

### [GET] `/tryon/status/{job_id}` - 생성 상태 조회 (SSE)
**Header**: `Accept: text/event-stream`
**Stream Payload Response**
```text
event: processing
data: {"status": "processing", "estimated_seconds_left": 15}

event: completed
data: {"job_id": "post_uuid", "status": "completed", "result_image_url": "https://...", "credit_deducted": true}
```

### [GET] `/tryon/credits` - 크레딧 잔여 조회
**Response `200`**
```json
{
  "remaining": 3,
  "total_daily": 5,
  "reset_at": "2025-01-02T00:00:00+09:00"
}
```
*Note: `reset_at`은 KST(Asia/Seoul) 기준 자정으로 설정됩니다.*

---

## 5. 관리자 전용 (Admin & Backoffice)

> **Note**: 본 API들은 MVP 운영 기간 중 별도의 관리자 웹 프론트엔드가 없는 상태에서 백엔드 어드민 권한(`role=admin`)을 가진 관리자가 API 툴(Postman 등)을 통해 운영하기 위한 특수 목적 연동 규격입니다.

### [GET] `/admin/reports` - 접수된 신고 목록 조회
**Response `200`**
```json
{
  "reports": [
    {
      "report_id": "uuid",
      "post_id": "post_uuid",
      "reporter_id": "user_uuid",
      "reason": "부적절한 이미지",
      "created_at": "ISO8601"
    }
  ]
}
```

### [PATCH] `/admin/posts/{post_id}/blind` - 게시물 강제 블라인드 (신고 조치)
**Request**: `(Headers Only)`
**Response `200`** (성공 시 해당 게시물 `is_blinded=true` 전환)

### [PATCH] `/admin/posts/{post_id}/unblind` - 블라인드 해제 (복구)
**Request**: `(Headers Only)`
**Response `200`** (성공 시 해당 게시물 `is_blinded=false` 전환하여 피드 노출 복구)

### [PATCH] `/admin/users/{user_id}/suspend` - 악성 유저 정지 / 징계
**Request**
```json
{
  "reason": "상습 욕설 및 가이드 위반",
  "is_active": false
}
```
**Response `200`** (유저 상태를 비활성(`is_active=false`)으로 변환)

### [POST] `/admin/users/{user_id}/credits` - 크레딧 수동 지급 (CS 보상)
**Request**
```json
{
  "granted_amount": 1,
  "reason": "서버 오류로 인한 클레임 보상 지급"
}
```
**Response `201`**
```json
{
  "remaining": 4,
  "message": "보상 처리가 성공적으로 완료되었습니다."
}
```

---

## 6. 전체 에러 코드 상세 (Error Codes)

| 에러 코드 | 응답 Status | 원인 | 노출 메시지 지정 가이드 |
|-----------|-------------|------|-------------------------|
| `ERR-000` | `500` | 예측 불가능 서버 에러 | 일시적인 오류가 발생했어요. 잠시 후 다시 시도해주세요. |
| `ERR-001` | `401` | 토큰 만료 또는 없음 | (로그인 화면으로 강제 이동) |
| `ERR-002` | `403` | 권한 없음 | 접근 권한이 없어요. |
| `ERR-003` | `404` | 리소스를 찾을 수 없음 | 찾을 수 없는 콘텐츠예요. |
| `ERR-101` | `500` | 이미지 스토리지 업로드 실패 | 사진 업로드에 실패했어요. |
| `ERR-103-A`| `422` | 시착 크레딧 잔여 0회 | 오늘 무료 시착을 모두 사용했어요. |
| `ERR-103-B`| `500` | AI 백엔드 플로우 생성 실패 | 시착 생성에 실패했어요. 크레딧은 보존됩니다. |
| `ERR-103-C`| `408` | AI 타임아웃 (30초) | 생성 시간이 초과됐어요. 다시 시도할게요. |
| `ERR-103-D`| `422` | 부적절한 이미지 감지됨 | 사용할 수 없는 안전하지 않은 이미지예요. |
| `ERR-201-A`| `422` | 옷장 용량 상한 초과(200개) | 옷장이 가득 찼어요 (최대 200개). |
| `ERR-201-B`| `422` | 옷 이미지 10MB 초과 | 이미지는 10MB 이하만 가능해요. |
| `ERR-204-A`| `200` | Vision 검색 결과 없음 | 비슷한 제품 출처를 찾지 못했어요. |
| `ERR-301-A`| `422` | 이미지 누락 업로드 시도 | (업로드 버튼 비활성화) |
| `ERR-301-B`| `422` | 일반 피드 이미지 20MB 초과 | 이미지는 20MB 이하만 가능해요. |
| `ERR-301-C`| `500` | DB 게시물 연결 실패 | 업로드에 실패했어요. 다시 시도해주세요. |
| `ERR-301-D`| `422` | 게시물 이미지 5장 초과 | 이미지는 최대 5장까지 첨부할 수 있어요. |
| `ERR-304-B`| `500` | 좋아요 API 타임아웃/실패 | (낙관적 UI 롤백 후 표출) 잠시 후 다시 시도해주세요. |
| `ERR-305-B`| `422` | 댓글 제한(200자) 초과 | (입력 차단 및 카운터 경고 UI) |
| `ERR-307-A`| `422` | 태그 10개 초과 | 태그는 최대 10개까지 가능해요. |
| `ERR-307-B`| `500` | Vision API 태그 분석 실패 | 자동 태그 추출에 실패했어요. 수동으로 입력해주세요. |
| `ERR-401-A`| `422` | 동일 게시물 중복 신고 | 이미 신고한 게시물이에요. |
