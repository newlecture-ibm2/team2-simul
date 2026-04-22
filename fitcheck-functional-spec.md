# FitCheck 기능 명세서

> 대상 독자: 프론트엔드 / 백엔드 개발자  
> 기준 문서: FitCheck MVP 요구사항 기능서 v1.0  
> 플랫폼: iOS / Android Native

---

## 목차

1. [문서 규칙](#1-문서-규칙)
2. [화면 ID / 기능 ID 체계](#2-화면-id--기능-id-체계)
3. [AI 가상시착 (P1)](#3-ai-가상시착-p1)
4. [개인 옷장 (P2)](#4-개인-옷장-p2)
5. [커뮤니티 피드 (P3)](#5-커뮤니티-피드-p3)
6. [제품 태그 & 쇼핑 연결 (P4)](#6-제품-태그--쇼핑-연결-p4)
7. [계정 & 프로필](#7-계정--프로필)
8. [공통 데이터 모델](#8-공통-데이터-모델)
9. [API 엔드포인트 정의](#9-api-엔드포인트-정의)
10. [에러 코드 & 예외 처리](#10-에러-코드--예외-처리)

---

## 1. 문서 규칙

### 우선순위 레이블
| 레이블 | 의미 |
|--------|------|
| `[필수]` | MVP 출시 전 반드시 구현 |
| `[선택]` | MVP 이후 추가 가능 |
| `[MVP제외]` | 명시적으로 이번 범위 외 |

### 상태 코드 규칙
- 성공: `200 OK` / `201 Created`
- 인증 오류: `401 Unauthorized`
- 권한 오류: `403 Forbidden`
- 리소스 없음: `404 Not Found`
- 유효성 오류: `422 Unprocessable Entity`
- 서버 오류: `500 Internal Server Error`

---

## 2. 화면 ID / 기능 ID 체계

### 화면 ID (SCR)

| 화면 ID | 화면명 | 진입 경로 |
|---------|--------|-----------|
| SCR-001 | 스플래시 | 앱 최초 실행 |
| SCR-002 | 온보딩 | 최초 로그인 전 |
| SCR-003 | 로그인 | 온보딩 → 로그인 버튼 |
| SCR-010 | 홈 피드 | 로그인 완료 후 |
| SCR-011 | 게시물 상세 | 피드 그리드 탭 |
| SCR-012 | 게시물 작성 | 하단 탭 '+' 버튼 |
| SCR-020 | AI 시착 홈 | 하단 탭 '시착' 버튼 |
| SCR-021 | 시착 사진 선택 | SCR-020 → 시작 버튼 |
| SCR-022 | 시착 옷 선택 | SCR-021 완료 후 |
| SCR-023 | 시착 생성 중 | SCR-022 → 시착하기 |
| SCR-024 | 시착 결과 | 생성 완료 후 |
| SCR-030 | 개인 옷장 | 프로필 → 옷장 버튼 |
| SCR-031 | 옷장 아이템 상세 | SCR-030 → 아이템 탭 |
| SCR-032 | 아이템 추가 | SCR-030 → '+' 버튼 |
| SCR-040 | 제품 태그 추가 | SCR-012 → 태그 추가 |
| SCR-041 | 태그 이미지 검색 | SCR-040 → 이미지 검색 |
| SCR-050 | 프로필 | 하단 탭 '프로필' |
| SCR-051 | 프로필 편집 | SCR-050 → 편집 |
| SCR-052 | 설정 | SCR-050 → 설정 |
| SCR-060 | 인앱 브라우저 | 구매하기 버튼 탭 |

### 기능 ID (FN)

| 기능 ID | 기능명 | 관련 화면 |
|---------|--------|-----------|
| FN-101 | 사진 입력 | SCR-021 |
| FN-102 | 시착 옷 선택 | SCR-022 |
| FN-103 | AI 이미지 생성 | SCR-023 |
| FN-104 | 시착 결과 저장/공유 | SCR-024 |
| FN-201 | 아이템 추가 | SCR-032 |
| FN-202 | 아이템 목록 조회 | SCR-030 |
| FN-203 | 아이템 편집/삭제 | SCR-031 |
| FN-301 | 게시물 작성 | SCR-012 |
| FN-302 | 피드 조회 | SCR-010 |
| FN-303 | 게시물 상세 조회 | SCR-011 |
| FN-304 | 좋아요 | SCR-011 |
| FN-305 | 댓글 | SCR-011 |
| FN-306 | 팔로우/언팔로우 | SCR-050 |
| FN-401 | 태그 이미지 검색 | SCR-041 |
| FN-402 | 태그 생성 | SCR-040 |
| FN-403 | 태그 삭제 | SCR-040 |
| FN-404 | 쇼핑 연결 | SCR-060 |
| FN-501 | 소셜 로그인 | SCR-003 |
| FN-502 | 프로필 편집 | SCR-051 |
| FN-503 | 알림 설정 | SCR-052 |

---

## 3. AI 가상시착 `P1`

### 3-1. 화면별 UI 컴포넌트

#### SCR-020 AI 시착 홈
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| HeaderBar | View | 타이틀 "가상시착", 크레딧 잔여 표시 |
| CreditBadge | Badge | 오늘 남은 무료 시착 횟수 (예: 3/5) |
| StartButton | Button | `[필수]` "시착 시작하기" CTA |
| RecentResultList | HorizontalScrollView | 최근 시착 결과 썸네일 최대 10개 |
| EmptyState | View | 시착 이력 없을 때 안내 문구 + 일러스트 |

#### SCR-021 시착 사진 선택
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| GalleryPicker | ImagePicker | 갤러리 접근, 단일 선택 |
| CameraButton | Button | 카메라 직접 촬영 진입 |
| GuideOverlay | View | 권장 포즈 가이드 반투명 오버레이 |
| ConfirmButton | Button | 사진 선택 완료 → SCR-022 이동 |

#### SCR-022 시착 옷 선택
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| TabBar | SegmentedControl | 피드 상품 / 내 옷장 / URL 입력 3탭 |
| FeedProductGrid | GridView | 피드에서 태그된 상품 목록 |
| ClosetItemGrid | GridView | 내 옷장 아이템 목록 |
| URLInputField | TextInput | 외부 URL 붙여넣기 입력창 |
| SelectedItemPreview | ImageView | 선택된 옷 이미지 미리보기 |
| TryOnButton | Button | "시착하기" → FN-103 실행 |

#### SCR-023 시착 생성 중
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| ProgressAnimation | LottieView | 로딩 애니메이션 |
| StatusText | Text | 처리 단계 안내 텍스트 |
| BrowseButton | Button | "다른 상품 보는 동안 기다리기" → 피드로 이동 |
| CancelButton | Button | 시착 취소 (크레딧 미차감) |

#### SCR-024 시착 결과
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| CompareSlider | SwipeView | 원본 ↔ 결과 비교 슬라이더 |
| SaveButton | Button | 디바이스 저장 |
| AddToClosetButton | Button | 옷장에 아이템 추가 (원클릭) |
| ShareButton | Button | 피드 공유 → SCR-012 이동 |
| RetryButton | Button | 다른 옷으로 다시 시착 |

---

### 3-2. 기능 명세

#### FN-101 사진 입력

**설명:** 사용자의 본인 사진을 선택하거나 촬영하여 시착에 사용할 베이스 이미지를 확보한다.

**입력 조건**
- 이미지 포맷: JPG, PNG, HEIC
- 최소 해상도: 512 × 512px
- 최대 파일 크기: 20MB

**처리 흐름**
1. 사용자가 갤러리 또는 카메라로 사진 선택
2. 클라이언트에서 이미지 리사이즈 (최대 1080px 기준)
3. 서버 업로드 → 임시 스토리지 저장
4. 업로드 완료 후 임시 URL 반환 → SCR-022로 이동

**에러 케이스**
| 케이스 | 처리 |
|--------|------|
| 파일 크기 초과 | 업로드 전 클라이언트 단 차단, 안내 토스트 |
| 미지원 포맷 | 클라이언트 단 차단, 안내 메시지 |
| 업로드 실패 (네트워크) | 재시도 버튼 노출, `ERR-101` |

---

#### FN-103 AI 이미지 생성

**설명:** 사용자 사진과 선택한 의류 이미지를 AI 모델에 전달하여 합성 이미지를 생성한다.

**입력값**
- `user_image_url`: 임시 저장된 사용자 사진 URL
- `clothing_image_url`: 선택한 의류 이미지 URL

**처리 흐름**
1. 크레딧 잔여 확인 (0이면 `ERR-103-A` 반환)
2. AI 생성 API 비동기 호출
3. 폴링 방식으로 생성 상태 확인 (5초 간격)
4. 완료 시 결과 이미지 URL 반환 → SCR-024 이동
5. 크레딧 1 차감 (생성 성공 시에만)
6. 사용자 원본 사진 서버에서 즉시 삭제

**제약**
- 처리 시간 목표: 30초 이내 (95th percentile)
- 자동 재시도: 1회 (타임아웃 시)
- 결과물 보관: 30일 (미저장 시 자동 삭제)

**에러 케이스**
| 케이스 | 에러 코드 | 처리 |
|--------|-----------|------|
| 크레딧 소진 | `ERR-103-A` | 크레딧 충전 안내 바텀시트 |
| AI 생성 실패 | `ERR-103-B` | 자동 재시도 1회 → 실패 시 안내 + 크레딧 미차감 |
| 타임아웃 (30초 초과) | `ERR-103-C` | 자동 재시도 1회 |
| 부적절한 이미지 감지 | `ERR-103-D` | 생성 거부 안내, 크레딧 미차감 |

---

### 3-3. API 엔드포인트 (AI 시착)

#### POST `/v1/tryon/upload`
사용자 사진 업로드

**Request**
```
Content-Type: multipart/form-data

{
  "image": File  // JPG, PNG, HEIC, max 20MB
}
```

**Response `201`**
```json
{
  "temp_image_url": "https://cdn.fitcheck.io/temp/abc123.jpg",
  "expires_at": "2025-01-01T00:30:00Z"
}
```

---

#### POST `/v1/tryon/generate`
AI 시착 생성 요청

**Request**
```json
{
  "user_image_url": "https://cdn.fitcheck.io/temp/abc123.jpg",
  "clothing_image_url": "https://cdn.fitcheck.io/items/xyz789.jpg"
}
```

**Response `201`**
```json
{
  "job_id": "job_abc123",
  "status": "processing",
  "estimated_seconds": 20
}
```

---

#### GET `/v1/tryon/status/{job_id}`
생성 상태 폴링

**Response `200`**
```json
{
  "job_id": "job_abc123",
  "status": "completed",  // processing | completed | failed
  "result_image_url": "https://cdn.fitcheck.io/results/result123.jpg",
  "credit_deducted": true
}
```

---

#### GET `/v1/tryon/credits`
오늘 남은 크레딧 조회

**Response `200`**
```json
{
  "remaining": 3,
  "total_daily": 5,
  "reset_at": "2025-01-02T00:00:00Z"
}
```

---

## 4. 개인 옷장 `P2`

### 4-1. 화면별 UI 컴포넌트

#### SCR-030 개인 옷장
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| CategoryTabBar | SegmentedControl | 전체 / 상의 / 하의 / 아우터 / 신발 / 액세서리 |
| ColorFilterBar | HorizontalScrollView | 색상 팔레트 칩 필터 |
| SortDropdown | Dropdown | 최근 추가순 / 자주 시착순 |
| ViewToggle | IconButton | 그리드 뷰 ↔ 리스트 뷰 전환 |
| ItemGrid | GridView | 저장된 아이템 카드 (이미지 + 브랜드명) |
| AddButton | FAB | 우하단 '+' 아이템 추가 버튼 |
| ItemCountBadge | Text | "총 N개" 아이템 수 표시 |
| EmptyState | View | 아이템 없을 때 안내 |

#### SCR-031 아이템 상세
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| ItemImage | ImageView | 아이템 이미지 (풀뷰) |
| MetaInfo | View | 카테고리, 색상, 브랜드명, 메모 |
| TryOnButton | Button | "이 옷으로 시착하기" |
| ShopLinkButton | Button | 외부 쇼핑몰 링크 (URL 있을 때만 노출) |
| EditButton | IconButton | 아이템 편집 |
| DeleteButton | IconButton | 아이템 삭제 (확인 다이얼로그) |

#### SCR-032 아이템 추가
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| ImageUploadArea | TouchableArea | 이미지 업로드 또는 URL 입력 선택 |
| CategoryPicker | Picker | 카테고리 선택 |
| ColorTagInput | TagInput | 색상 자동 추출 + 수동 편집 |
| BrandNameInput | TextInput | 브랜드명 (선택) |
| ShopURLInput | TextInput | 외부 쇼핑몰 URL (선택) |
| MemoInput | TextInput | 개인 메모 최대 100자 (선택) |
| SaveButton | Button | 저장 → FN-201 실행 |

---

### 4-2. 기능 명세

#### FN-201 아이템 추가

**설명:** 패션 아이템을 옷장에 저장하고 메타데이터를 등록한다.

**3가지 추가 경로**

| 경로 | 트리거 | 처리 |
|------|--------|------|
| 시착 결과에서 추가 | SCR-024 "옷장에 추가" 버튼 | 시착에 사용한 의류 이미지 자동 세팅 |
| 피드 태그에서 저장 | SCR-011 상품 태그 "저장" | 태그 이미지 + 쇼핑 URL 자동 세팅 |
| 직접 추가 | SCR-032 | 이미지 업로드 또는 URL 직접 입력 |

**저장 조건**
- 이미지 필수 (URL 크롤링 또는 직접 업로드)
- 카테고리 필수
- 색상: 자동 추출 후 사용자 확인 (변경 가능)
- 저장 상한: 계정당 200개 (초과 시 `ERR-201-A`)

**에러 케이스**
| 케이스 | 에러 코드 | 처리 |
|--------|-----------|------|
| 저장 상한 초과 | `ERR-201-A` | "옷장이 가득 찼어요" 안내, 삭제 유도 |
| 이미지 크기 초과 | `ERR-201-B` | 클라이언트 단 차단 (max 10MB) |
| URL 크롤링 실패 | `ERR-201-C` | 직접 이미지 업로드로 fallback 안내 |

---

#### FN-203 아이템 삭제

**설명:** 옷장에서 아이템을 삭제한다.

**처리 흐름**
1. 삭제 버튼 탭 → 확인 다이얼로그 ("정말 삭제할까요?")
2. 확인 시 소프트 딜리트 처리 (DB `deleted_at` 세팅)
3. 연결된 시착 결과 이력은 유지 (아이템만 옷장에서 제거)

---

### 4-3. API 엔드포인트 (옷장)

#### POST `/v1/closet/items`
아이템 추가

**Request**
```json
{
  "image_url": "https://cdn.fitcheck.io/items/abc.jpg",
  "category": "top",           // top | bottom | outer | shoes | accessory
  "color_tags": ["white", "black"],
  "brand_name": "무신사 스탠다드",  // optional
  "shop_url": "https://...",   // optional
  "memo": "여름용"              // optional, max 100자
}
```

**Response `201`**
```json
{
  "item_id": "item_abc123",
  "created_at": "2025-01-01T00:00:00Z"
}
```

---

#### GET `/v1/closet/items`
아이템 목록 조회

**Query Parameters**
```
category=top          // optional
color=white           // optional
sort=recent           // recent | most_tried
page=1
per_page=20
```

**Response `200`**
```json
{
  "items": [
    {
      "item_id": "item_abc123",
      "image_url": "https://...",
      "category": "top",
      "color_tags": ["white"],
      "brand_name": "무신사 스탠다드",
      "try_count": 3,
      "created_at": "2025-01-01T00:00:00Z"
    }
  ],
  "total": 42,
  "page": 1,
  "per_page": 20
}
```

---

#### DELETE `/v1/closet/items/{item_id}`
아이템 삭제

**Response `200`**
```json
{
  "item_id": "item_abc123",
  "deleted_at": "2025-01-01T00:00:00Z"
}
```

---

## 5. 커뮤니티 피드 `P3`

### 5-1. 화면별 UI 컴포넌트

#### SCR-010 홈 피드
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| TopTabBar | SegmentedControl | 전체 / 팔로잉 탭 |
| SortToggle | SegmentedControl | 최신순 / 인기순 (24h 조회수 기준) |
| FeedGrid | GridView | 2열 이미지 그리드, 무한 스크롤 |
| PostCard | View | 썸네일 이미지 + 쇼핑백 아이콘 (태그 있을 때) |
| FloatingPostButton | FAB | 우하단 게시물 작성 버튼 |

#### SCR-011 게시물 상세
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| ImageCarousel | SwipeView | 이미지 최대 5장 스와이프 |
| TagPinLayer | OverlayView | 이미지 탭 시 태그 핀 표시/숨김 토글 |
| ProductListSection | View | 태그된 상품 리스트 (브랜드 + 제품명 + 구매하기) |
| TryOnBanner | Button | "이 옷으로 AI 시착 해보기" |
| AuthorRow | View | 프로필 이미지 + 닉네임 + 팔로우 버튼 |
| CaptionText | Text | 캡션 (300자, 더보기 접힘) |
| LikeButton | IconButton | 하트 아이콘 + 좋아요 수 |
| CommentSection | View | 댓글 목록 + 입력창 |
| ReportButton | IconButton | 신고 |

#### SCR-012 게시물 작성
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| ImagePickerRow | HorizontalScrollView | 이미지 추가 (최대 5장) |
| CaptionInput | TextArea | 캡션 입력, 300자 카운터 |
| TagAddButton | Button | `[필수]` 제품 태그 추가 → SCR-040 |
| TagList | View | 추가된 태그 목록 (삭제 가능) |
| VisibilityToggle | Toggle | 공개 / 비공개 |
| UploadButton | Button | 업로드 (태그 없으면 비활성화) |

---

### 5-2. 기능 명세

#### FN-301 게시물 작성

**입력 조건**
| 필드 | 필수 여부 | 제약 |
|------|-----------|------|
| 이미지 | 필수 | 1–5장, 장당 최대 20MB, JPG/PNG/HEIC |
| 제품 태그 | 필수 | 1개 이상 없으면 업로드 버튼 비활성화 |
| 캡션 | 선택 | 최대 300자 |
| 공개 여부 | 필수 | 기본값: 공개 |

**처리 흐름**
1. 이미지 최대 5장 선택
2. 제품 태그 추가 (FN-402)
3. 캡션 입력 (선택)
4. 업로드 버튼 탭
5. 이미지 병렬 업로드 → 게시물 생성 API 호출
6. 완료 후 피드로 이동

**에러 케이스**
| 케이스 | 에러 코드 | 처리 |
|--------|-----------|------|
| 태그 없이 업로드 시도 | `ERR-301-A` | 업로드 버튼 비활성화 + 안내 문구 |
| 이미지 장당 용량 초과 | `ERR-301-B` | 해당 이미지 선택 차단, 안내 토스트 |
| 업로드 실패 | `ERR-301-C` | 재시도 버튼 노출 |

---

#### FN-304 좋아요

**처리 흐름**
1. 하트 버튼 탭
2. 낙관적 업데이트 (즉시 UI 반영)
3. API 호출 실패 시 롤백

**에러 케이스**
| 케이스 | 에러 코드 | 처리 |
|--------|-----------|------|
| 비로그인 상태 | `ERR-304-A` | 로그인 유도 바텀시트 |
| 네트워크 오류 | `ERR-304-B` | UI 롤백 + 토스트 |

---

#### FN-305 댓글

**제약**
- 텍스트 최대 200자
- 최대 2 depth (대댓글까지)
- 대댓글에는 추가 답글 불가

**에러 케이스**
| 케이스 | 에러 코드 | 처리 |
|--------|-----------|------|
| 비로그인 상태 | `ERR-305-A` | 로그인 유도 바텀시트 |
| 200자 초과 | `ERR-305-B` | 입력 차단 + 카운터 빨간색 표시 |

---

### 5-3. API 엔드포인트 (피드)

#### POST `/v1/posts`
게시물 생성

**Request**
```json
{
  "image_urls": [
    "https://cdn.fitcheck.io/posts/img1.jpg"
  ],
  "caption": "오늘 코디",          // optional, max 300자
  "tag_ids": ["tag_abc", "tag_xyz"], // 1개 이상 필수
  "is_public": true
}
```

**Response `201`**
```json
{
  "post_id": "post_abc123",
  "created_at": "2025-01-01T00:00:00Z"
}
```

---

#### GET `/v1/posts`
피드 목록 조회

**Query Parameters**
```
tab=all             // all | following
sort=recent         // recent | popular
page=1
per_page=20
```

**Response `200`**
```json
{
  "posts": [
    {
      "post_id": "post_abc123",
      "thumbnail_url": "https://...",
      "has_tags": true,
      "like_count": 42,
      "author": {
        "user_id": "user_xyz",
        "nickname": "패션러버",
        "profile_image_url": "https://..."
      },
      "created_at": "2025-01-01T00:00:00Z"
    }
  ],
  "total": 200,
  "page": 1,
  "per_page": 20
}
```

---

#### POST `/v1/posts/{post_id}/likes`
좋아요 토글

**Response `200`**
```json
{
  "liked": true,
  "like_count": 43
}
```

---

#### GET `/v1/posts/{post_id}/comments`
댓글 목록 조회

**Response `200`**
```json
{
  "comments": [
    {
      "comment_id": "cmt_abc",
      "user": { "user_id": "user_xyz", "nickname": "패션러버" },
      "content": "어디서 샀어요?",
      "depth": 1,
      "parent_comment_id": null,
      "created_at": "2025-01-01T00:00:00Z",
      "replies": [
        {
          "comment_id": "cmt_def",
          "depth": 2,
          "parent_comment_id": "cmt_abc",
          "content": "무신사에서요!",
          "created_at": "2025-01-01T00:01:00Z"
        }
      ]
    }
  ]
}
```

---

#### POST `/v1/posts/{post_id}/comments`
댓글 작성

**Request**
```json
{
  "content": "어디서 샀어요?",     // max 200자
  "parent_comment_id": null        // 대댓글이면 부모 comment_id
}
```

---

## 6. 제품 태그 & 쇼핑 연결 `P4`

### 6-1. 화면별 UI 컴포넌트

#### SCR-040 제품 태그 추가
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| ImagePreview | ImageView | 태그 추가할 게시물 이미지 |
| TagPinLayer | OverlayView | 이미지 위 핀 위치 지정 (탭으로 핀 추가) |
| TagFormBottomSheet | BottomSheet | 상품 정보 입력 폼 |
| ImageSearchButton | Button | 이미지로 검색 → SCR-041 |
| ManualInputFields | View | 브랜드명 + URL 직접 입력 (fallback) |
| TagCountBadge | Badge | "N/5" 현재 태그 수 |

#### SCR-041 태그 이미지 검색
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| ImageUploadArea | TouchableArea | 캡처 이미지 업로드 |
| SearchButton | Button | `[필수]` 명시적 탭 시에만 API 호출 |
| LoadingState | View | 검색 중 로딩 표시 |
| ResultList | ListView | 유사 상품 결과 리스트 (이미지 + 상품명 + 출처) |
| SelectButton | Button | 상품 선택 → 태그 자동 완성 |
| FallbackLink | TextButton | "직접 입력하기" fallback |

---

### 6-2. 기능 명세

#### FN-401 태그 이미지 검색

**설명:** 사용자가 업로드한 캡처 이미지를 Google Vision API Web Detection으로 분석하여 유사 상품 페이지 목록을 반환한다.

**처리 흐름**
1. 사용자가 이미지 업로드
2. **"검색" 버튼 명시적 탭 시에만** 서버로 이미지 전송 (자동 호출 금지)
3. 서버가 Google Vision API Web Detection 호출
4. 동일 이미지 해시 캐시 확인 → 캐시 히트 시 API 미호출
5. 결과 상품 리스트 반환 (최대 10개)
6. 사용자가 상품 선택 → 태그 폼 자동 완성

**API 비용 최적화**
- 이미지 MD5 해시 기반 캐싱 (TTL: 24시간)
- '검색' 버튼 탭 시에만 호출 (이미지 업로드 자동 호출 금지)
- Google Vision API Web Detection: 월 1,000건 무료 / 초과 시 1,000건당 $3.50

**에러 케이스**
| 케이스 | 에러 코드 | 처리 |
|--------|-----------|------|
| 인식 결과 없음 | `ERR-401-A` | "유사 상품을 찾지 못했어요" + 직접 입력 유도 |
| API 호출 실패 | `ERR-401-B` | "검색에 실패했어요. 다시 시도해주세요" + 직접 입력 유도 |
| 이미지 크기 초과 | `ERR-401-C` | 클라이언트 단 차단 (max 10MB) |

---

#### FN-402 태그 생성

**설명:** 이미지 위 특정 좌표에 상품 정보를 연결하는 태그 핀을 생성한다.

**입력값**
| 필드 | 필수 | 설명 |
|------|------|------|
| position_x | 필수 | 이미지 내 X 좌표 (0.0 ~ 1.0 비율) |
| position_y | 필수 | 이미지 내 Y 좌표 (0.0 ~ 1.0 비율) |
| brand_name | 선택 | 브랜드명 |
| product_name | 선택 | 상품명 |
| shop_url | 필수 | 외부 쇼핑몰 URL (http/https 필수) |
| image_index | 필수 | 게시물 내 이미지 순서 (0-based) |

**제약**
- 이미지당 최대 5개 태그
- shop_url: URL 형식 유효성 검사 + 블랙리스트 필터

**에러 케이스**
| 케이스 | 에러 코드 | 처리 |
|--------|-----------|------|
| 태그 5개 초과 | `ERR-402-A` | "이미지당 태그는 최대 5개예요" 토스트 |
| 잘못된 URL 형식 | `ERR-402-B` | 인라인 에러 메시지 |
| 블랙리스트 URL | `ERR-402-C` | "연결할 수 없는 링크예요" 안내 |

---

#### FN-403 태그 삭제

**설명:** 태그 삭제는 기본적으로 불가하며, 조건 충족 시에만 등록자가 삭제할 수 있다.

**삭제 가능 조건 (AND 조건)**
1. 삭제 요청자 = 태그 등록자
2. 해당 태그가 등록된 게시물 수 ≤ 1

**처리 흐름**
1. 태그 삭제 요청 수신
2. 요청자 = 등록자 검증 → 불일치 시 `ERR-403-A`
3. 해당 태그가 연결된 게시물 수 조회
4. 게시물 수 > 1 이면 `ERR-403-B` 반환
5. 조건 충족 시 소프트 딜리트 처리

**에러 케이스**
| 케이스 | 에러 코드 | 처리 |
|--------|-----------|------|
| 등록자 아닌 사용자 요청 | `ERR-403-A` | `403 Forbidden` |
| 게시물 2개 이상 연결 | `ERR-403-B` | "이 태그는 다른 게시물에서도 사용 중이라 삭제할 수 없어요" |

---

### 6-3. API 엔드포인트 (태그)

#### POST `/v1/tags/search`
이미지로 유사 상품 검색

**Request**
```
Content-Type: multipart/form-data

{
  "image": File,         // max 10MB
  "image_hash": "md5_hash_string"  // 캐시 확인용
}
```

**Response `200`**
```json
{
  "cached": false,
  "results": [
    {
      "product_name": "오버핏 셔츠",
      "brand_name": "무신사 스탠다드",
      "shop_url": "https://www.musinsa.com/...",
      "thumbnail_url": "https://...",
      "source": "musinsa.com"
    }
  ]
}
```

---

#### POST `/v1/tags`
태그 생성

**Request**
```json
{
  "post_id": "post_abc123",
  "image_index": 0,
  "position_x": 0.45,
  "position_y": 0.32,
  "brand_name": "무신사 스탠다드",
  "product_name": "오버핏 셔츠",
  "shop_url": "https://www.musinsa.com/..."
}
```

**Response `201`**
```json
{
  "tag_id": "tag_abc123",
  "created_at": "2025-01-01T00:00:00Z"
}
```

---

#### DELETE `/v1/tags/{tag_id}`
태그 삭제 (조건부)

**Response `200`**
```json
{
  "tag_id": "tag_abc123",
  "deleted_at": "2025-01-01T00:00:00Z"
}
```

**Response `403` — 조건 미충족**
```json
{
  "error_code": "ERR-403-B",
  "message": "이 태그는 다른 게시물에서도 사용 중이라 삭제할 수 없어요"
}
```

---

## 7. 계정 & 프로필

### 7-1. 화면별 UI 컴포넌트

#### SCR-003 로그인
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| KakaoLoginButton | Button | 카카오 소셜 로그인 |
| NaverLoginButton | Button | 네이버 소셜 로그인 |
| GoogleLoginButton | Button | 구글 소셜 로그인 |
| EmailLoginLink | TextButton | 이메일 로그인 (선택) |
| PrivacyPolicyLink | TextButton | 개인정보처리방침 동의 안내 |

#### SCR-050 프로필
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| ProfileHeader | View | 프로필 이미지 + 닉네임 + 한줄 소개 |
| StatsRow | View | 팔로워 / 팔로잉 / 게시물 수 |
| ClosetButton | Button | 내 옷장 바로가기 |
| PostGrid | GridView | 내 게시물 그리드 |
| EditButton | Button | 프로필 편집 → SCR-051 |
| SettingButton | IconButton | 설정 → SCR-052 |

---

### 7-2. API 엔드포인트 (계정)

#### POST `/v1/auth/social`
소셜 로그인

**Request**
```json
{
  "provider": "kakao",   // kakao | naver | google
  "access_token": "oauth_access_token"
}
```

**Response `200`**
```json
{
  "access_token": "jwt_token",
  "refresh_token": "refresh_token",
  "user": {
    "user_id": "user_abc123",
    "nickname": "패션러버",
    "profile_image_url": "https://...",
    "is_new_user": false
  }
}
```

---

#### PATCH `/v1/users/me`
프로필 수정

**Request**
```json
{
  "nickname": "새닉네임",          // optional
  "bio": "패션을 사랑해요",         // optional, max 50자
  "profile_image_url": "https://..." // optional
}
```

---

#### GET `/v1/users/{user_id}/posts`
사용자 게시물 목록

**Response `200`**
```json
{
  "posts": [ /* PostCard 배열 */ ],
  "total": 15
}
```

---

## 8. 공통 데이터 모델

### Users
```
users
├── user_id          UUID, PK
├── provider         ENUM (kakao, naver, google, email)
├── provider_id      VARCHAR, 소셜 고유 ID
├── nickname         VARCHAR(20), NOT NULL
├── bio              VARCHAR(50)
├── profile_image_url TEXT
├── is_public        BOOLEAN, DEFAULT true
├── created_at       TIMESTAMP
└── deleted_at       TIMESTAMP, nullable (탈퇴)
```

### Posts
```
posts
├── post_id          UUID, PK
├── user_id          UUID, FK → users
├── caption          VARCHAR(300)
├── is_public        BOOLEAN, DEFAULT true
├── like_count       INT, DEFAULT 0
├── view_count       INT, DEFAULT 0
├── created_at       TIMESTAMP
└── deleted_at       TIMESTAMP, nullable
```

### Post Images
```
post_images
├── image_id         UUID, PK
├── post_id          UUID, FK → posts
├── image_url        TEXT, NOT NULL
├── image_index      INT (0-based 순서)
└── created_at       TIMESTAMP
```

### Tags
```
tags
├── tag_id           UUID, PK
├── created_by       UUID, FK → users (등록자)
├── brand_name       VARCHAR(100)
├── product_name     VARCHAR(200)
├── shop_url         TEXT, NOT NULL
├── created_at       TIMESTAMP
└── deleted_at       TIMESTAMP, nullable
```

### Post Tags (태그 ↔ 게시물 연결)
```
post_tags
├── post_tag_id      UUID, PK
├── post_id          UUID, FK → posts
├── tag_id           UUID, FK → tags
├── image_index      INT
├── position_x       FLOAT (0.0 ~ 1.0)
└── position_y       FLOAT (0.0 ~ 1.0)
```

### Closet Items
```
closet_items
├── item_id          UUID, PK
├── user_id          UUID, FK → users
├── image_url        TEXT, NOT NULL
├── category         ENUM (top, bottom, outer, shoes, accessory)
├── color_tags       VARCHAR[] (배열)
├── brand_name       VARCHAR(100)
├── shop_url         TEXT
├── memo             VARCHAR(100)
├── try_count        INT, DEFAULT 0
├── created_at       TIMESTAMP
└── deleted_at       TIMESTAMP, nullable
```

### Tryon Results
```
tryon_results
├── result_id        UUID, PK
├── user_id          UUID, FK → users
├── clothing_image_url TEXT
├── result_image_url   TEXT
├── status           ENUM (processing, completed, failed)
├── created_at       TIMESTAMP
└── expires_at       TIMESTAMP (30일 후 자동 삭제)
```

### Tryon Credits
```
tryon_credits
├── credit_id        UUID, PK
├── user_id          UUID, FK → users
├── used_at          TIMESTAMP
└── job_id           UUID, FK → tryon_results
```

### Comments
```
comments
├── comment_id       UUID, PK
├── post_id          UUID, FK → posts
├── user_id          UUID, FK → users
├── parent_comment_id UUID, nullable, FK → comments (대댓글)
├── depth            INT (1 or 2)
├── content          VARCHAR(200), NOT NULL
├── created_at       TIMESTAMP
└── deleted_at       TIMESTAMP, nullable
```

### Follows
```
follows
├── follow_id        UUID, PK
├── follower_id      UUID, FK → users
├── following_id     UUID, FK → users
└── created_at       TIMESTAMP
```

---

## 9. API 엔드포인트 정의

### 전체 엔드포인트 목록

| Method | Path | 기능 | 인증 |
|--------|------|------|------|
| POST | `/v1/auth/social` | 소셜 로그인 | 불필요 |
| POST | `/v1/auth/refresh` | 토큰 갱신 | 불필요 |
| DELETE | `/v1/auth/logout` | 로그아웃 | 필요 |
| GET | `/v1/users/me` | 내 정보 조회 | 필요 |
| PATCH | `/v1/users/me` | 프로필 수정 | 필요 |
| DELETE | `/v1/users/me` | 회원 탈퇴 | 필요 |
| GET | `/v1/users/{user_id}` | 사용자 프로필 조회 | 불필요 |
| GET | `/v1/users/{user_id}/posts` | 사용자 게시물 목록 | 불필요 |
| POST | `/v1/follows/{user_id}` | 팔로우 | 필요 |
| DELETE | `/v1/follows/{user_id}` | 언팔로우 | 필요 |
| GET | `/v1/posts` | 피드 목록 | 불필요 (비회원 열람) |
| POST | `/v1/posts` | 게시물 작성 | 필요 |
| GET | `/v1/posts/{post_id}` | 게시물 상세 | 불필요 |
| DELETE | `/v1/posts/{post_id}` | 게시물 삭제 | 필요 |
| POST | `/v1/posts/{post_id}/likes` | 좋아요 토글 | 필요 |
| GET | `/v1/posts/{post_id}/comments` | 댓글 목록 | 불필요 |
| POST | `/v1/posts/{post_id}/comments` | 댓글 작성 | 필요 |
| DELETE | `/v1/comments/{comment_id}` | 댓글 삭제 | 필요 |
| POST | `/v1/posts/{post_id}/report` | 게시물 신고 | 필요 |
| POST | `/v1/tags/search` | 이미지로 상품 검색 | 필요 |
| POST | `/v1/tags` | 태그 생성 | 필요 |
| DELETE | `/v1/tags/{tag_id}` | 태그 삭제 (조건부) | 필요 |
| GET | `/v1/closet/items` | 옷장 아이템 목록 | 필요 |
| POST | `/v1/closet/items` | 아이템 추가 | 필요 |
| GET | `/v1/closet/items/{item_id}` | 아이템 상세 | 필요 |
| PATCH | `/v1/closet/items/{item_id}` | 아이템 수정 | 필요 |
| DELETE | `/v1/closet/items/{item_id}` | 아이템 삭제 | 필요 |
| POST | `/v1/tryon/upload` | 시착 사진 업로드 | 필요 |
| POST | `/v1/tryon/generate` | 시착 생성 요청 | 필요 |
| GET | `/v1/tryon/status/{job_id}` | 생성 상태 폴링 | 필요 |
| GET | `/v1/tryon/results` | 시착 결과 목록 | 필요 |
| GET | `/v1/tryon/credits` | 크레딧 잔여 조회 | 필요 |

### 공통 Request Header
```
Authorization: Bearer {jwt_access_token}
Content-Type: application/json
Accept-Language: ko
```

### 공통 에러 Response 형식
```json
{
  "error_code": "ERR-101",
  "message": "사용자에게 노출되는 메시지",
  "detail": "개발자용 상세 메시지"
}
```

---

## 10. 에러 코드 & 예외 처리

### 전체 에러 코드 목록

| 에러 코드 | HTTP 상태 | 원인 | 사용자 메시지 |
|-----------|-----------|------|---------------|
| `ERR-000` | 500 | 알 수 없는 서버 오류 | "일시적인 오류가 발생했어요. 잠시 후 다시 시도해주세요" |
| `ERR-001` | 401 | 인증 토큰 없음/만료 | 로그인 화면으로 이동 |
| `ERR-002` | 403 | 권한 없음 | "접근 권한이 없어요" |
| `ERR-003` | 404 | 리소스 없음 | "찾을 수 없는 콘텐츠예요" |
| `ERR-101` | 500 | 사진 업로드 실패 | "사진 업로드에 실패했어요. 다시 시도해주세요" |
| `ERR-103-A` | 422 | 시착 크레딧 소진 | "오늘 무료 시착을 모두 사용했어요" |
| `ERR-103-B` | 500 | AI 생성 실패 | "시착 생성에 실패했어요. 크레딧은 차감되지 않았어요" |
| `ERR-103-C` | 408 | AI 생성 타임아웃 | "생성 시간이 초과됐어요. 다시 시도할게요" |
| `ERR-103-D` | 422 | 부적절한 이미지 | "사용할 수 없는 이미지예요" |
| `ERR-201-A` | 422 | 옷장 저장 상한 초과 | "옷장이 가득 찼어요 (최대 200개)" |
| `ERR-201-B` | 422 | 아이템 이미지 크기 초과 | "이미지는 10MB 이하만 가능해요" |
| `ERR-201-C` | 422 | URL 크롤링 실패 | "링크에서 이미지를 가져오지 못했어요. 직접 업로드해주세요" |
| `ERR-301-A` | 422 | 태그 없이 게시물 업로드 | 업로드 버튼 비활성화 + "제품 태그를 1개 이상 추가해주세요" |
| `ERR-301-B` | 422 | 게시물 이미지 크기 초과 | "이미지는 20MB 이하만 가능해요" |
| `ERR-301-C` | 500 | 게시물 업로드 실패 | "업로드에 실패했어요. 다시 시도해주세요" |
| `ERR-304-A` | 401 | 비로그인 좋아요 시도 | 로그인 유도 바텀시트 |
| `ERR-304-B` | 500 | 좋아요 네트워크 오류 | UI 롤백 + "잠시 후 다시 시도해주세요" |
| `ERR-305-A` | 401 | 비로그인 댓글 시도 | 로그인 유도 바텀시트 |
| `ERR-305-B` | 422 | 댓글 200자 초과 | 입력 차단 + 카운터 빨간색 표시 |
| `ERR-401-A` | 200 | Vision API 결과 없음 | "유사 상품을 찾지 못했어요. 직접 입력해주세요" |
| `ERR-401-B` | 500 | Vision API 호출 실패 | "검색에 실패했어요. 직접 입력해주세요" |
| `ERR-401-C` | 422 | 검색 이미지 크기 초과 | "이미지는 10MB 이하만 가능해요" |
| `ERR-402-A` | 422 | 태그 5개 초과 | "이미지당 태그는 최대 5개예요" |
| `ERR-402-B` | 422 | 잘못된 URL 형식 | "올바른 URL을 입력해주세요 (http:// 또는 https://)" |
| `ERR-402-C` | 422 | 블랙리스트 URL | "연결할 수 없는 링크예요" |
| `ERR-403-A` | 403 | 타인 태그 삭제 시도 | `403 Forbidden` |
| `ERR-403-B` | 422 | 게시물 2개 이상 연결 태그 삭제 시도 | "이 태그는 다른 게시물에서도 사용 중이라 삭제할 수 없어요" |

### 공통 예외 처리 원칙

1. **낙관적 업데이트**: 좋아요 등 빠른 인터랙션은 UI 먼저 반영 후 API 실패 시 롤백
2. **비로그인 접근**: 피드/상세 열람은 허용, 시착/저장/공유/좋아요/댓글은 로그인 유도 바텀시트
3. **네트워크 없음**: 전역 오프라인 배너 표시, 캐시된 데이터 표시 (읽기 전용)
4. **토큰 만료**: 자동 토큰 갱신 시도 → 실패 시 로그인 화면으로 이동
5. **서버 점검**: 503 응답 시 점검 안내 화면 표시
