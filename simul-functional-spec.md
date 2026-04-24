# Simul 기능 명세서

> 대상 독자: 프론트엔드 / 백엔드 개발자  
> 기준 문서: Simul MVP 요구사항 기능서 v1.0  
> 플랫폼: iOS / Android Native

---

## 목차

1. [문서 규칙](#1-문서-규칙)
2. [화면 ID / 기능 ID 체계](#2-화면-id--기능-id-체계)
3. [AI 가상시착](#3-ai-가상시착)
4. [개인 옷장](#4-개인-옷장)
5. [커뮤니티 피드](#5-커뮤니티-피드)
7. [계정 & 프로필](#7-계정--프로필)
8. [공통 데이터 모델](#8-공통-데이터-모델)
9. [API 엔드포인트 정의](#9-api-엔드포인트-정의)
10. [에러 코드 & 예외 처리](#10-에러-코드--예외-처리)

---

## 1. 문서 규칙

### 우선순위 레이블 (MoSCoW)
| 레이블 | 의미 |
|--------|------|
| `[Must]` | (Must-Have) MVP 출시 전 반드시 구현 |
| `[Should]` | (Should-Have) 중요하지만 필수 아님, 여력 시 포함 |
| `[Could]` | (Could-Have) MVP 이후 추가 가능 |
| `[Won't]` | (Won't-Have) 명시적으로 이번 범위 외 |

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
| SCR-050 | 프로필 | 하단 탭 '프로필' |
| SCR-051 | 프로필 편집 | SCR-050 → 편집 |
| SCR-052 | 설정 | SCR-050 → 설정 |
| SCR-060 | 인앱 브라우저 | 외부 웹사이트 링크 탭 |

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
| FN-501 | 소셜 로그인 | SCR-003 |
| FN-502 | 프로필 편집 | SCR-051 |
| FN-503 | 알림 설정 | SCR-052 |
| FN-901 | 콘텐츠 모더레이션 (게시물/댓글 블라인드) | API 전용 |
| FN-902 | 유저 정지 및 크레딧 수동 제어 | API 전용 |

---

## 3. AI 가상시착

### 3-1. 화면별 UI 컴포넌트

#### SCR-020 AI 시착 홈 [Must]
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| HeaderBar | View | 타이틀 "가상시착", 크레딧 잔여 표시 |
| CreditBadge | Badge | 오늘 남은 무료 시착 횟수 (예: 3/5) |
| StartButton | Button | `[Must]` "시착 시작하기" CTA |
| RecentResultList | HorizontalScrollView | 최근 시착 결과 썸네일 최대 10개 |
| EmptyState | View | 시착 이력 없을 때 안내 문구 + 일러스트 |

#### SCR-021 시착 사람 이미지(베이스) 선택 [Must]
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| BaseImageList | GridView | 이전에 등록/사용한 내 베이스 사람 이미지 목록 |
| UploadButton | Button | 새 베이스 이미지 기기 업로드 및 DB 등록 |
| TryonResultBtn | Button | 과거 시착 결과를 새 사람 이미지로 불러와 복제등록 |
| ConfirmButton | Button | 사진 선택 완료 → SCR-022 이동 |

#### SCR-022 시착 옷 선택 [Must]
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| TabBar | SegmentedControl | 내 옷장 / 새 이미지 업로드 / 피드 상품 3탭 |
| ClosetItemGrid | GridView | 내 옷장 아이템 목록 |
| ImageUploadField | TouchableArea | 기기에서 새 옷 이미지 직접 업로드 (시착 완료 시 옷장에 자동 저장) |
| FeedProductGrid | GridView | 피드에서 태그된 상품 목록 |
| SelectedItemPreview | ImageView | 선택된 옷 이미지 미리보기 |
| TryOnButton | Button | "시착하기" → FN-103 실행 |

#### SCR-023 시착 생성 중 [Must]
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| ProgressAnimation | LottieView | 로딩 애니메이션 |
| StatusText | Text | 처리 단계 안내 텍스트 |
| BrowseButton | Button | "다른 상품 보는 동안 기다리기" → 피드로 이동 |
| CancelButton | Button | 시착 취소 (크레딧 미차감) |

#### SCR-024 시착 결과 [Must]
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| CompareSlider | SwipeView | 원본 ↔ 결과 비교 슬라이더 |
| SaveButton | Button | 디바이스 저장 |
| PublishButton | Button | 공개 피드에 자랑하기 (비공개 게시물을 공개로 전환 및 짧은 캡션 입력 바텀시트) |
| RetryButton | Button | 다른 옷으로 다시 시착 |

---

### 3-2. 기능 명세

#### FN-101 사람 이미지(베이스) 선택 및 등록 [Must]

**설명:** 사용자의 본인 사진, 혹은 이전에 잘 나온 AI 시착결과물을 시착에 사용할 베이스 모델로 등록/선택한다.

**입력 조건**
- 이미지 포맷: JPG, PNG, HEIC
- 최소 해상도: 256 × 256px
- 최대 파일 크기: 20MB

**처리 흐름**
1. 저장된 나의 বে이스 이미지 목록 조회 후 선택
2. 신규 사진 업로드 시 스토리지 저장 및 `base_images` 테이블에 등록됨
3. 기존 AI 시착물을 베이스로 사용할 시, 해당 게시물의 데이터를 기반으로 `base_images` 테이블에 복제 등록됨
4. 최종 선택된 `base_image_id` 확보 후 SCR-022로 이동

**에러 케이스**
| 케이스 | 처리 |
|--------|------|
| 파일 크기 초과 | 업로드 전 클라이언트 단 차단, 안내 토스트 |
| 미지원 포맷 | 클라이언트 단 차단, 안내 메시지 |
| 업로드 실패 (네트워크) | 재시도 버튼 노출, `ERR-101` |

---

#### FN-103 AI 이미지 생성 [Must]

**설명:** 사용자 사진과 선택한 의류 이미지를 AI 모델에 전달하여 합성 이미지를 생성한다.

**입력값**
- `user_image_url`: 저장된 사용자 사진 URL
- `clothing_image_url`: 선택한 의류 이미지 URL

**처리 흐름**
1. 크레딧 잔여 확인 (0이면 `ERR-103-A` 반환)
2. AI 생성 API 비동기 호출 (job_id 반환)
3. SSE(Server-Sent Events) 스트림을 연결하여 실시간 상태 확인
4. 완료 수신 시 결과 이미지를 내 프로필의 "비공개 게시물(is_public=false)"로 자동 생성 후 SCR-024 이동
5. 크레딧 1 차감 (생성 성공 시에만)
6. 생성된 시착 게시글에 사용된 의류 출처(item_id)를 연결
7. 사용자 원본 사진은 삭제하지 않고 유지하여 다음 시착 시 재사용

**제약**
- 처리 시간 목표: 30초 이내 (95th percentile)
- 자동 재시도: 1회 (타임아웃 시)
- 결과물 보관: 비공개 게시물 형태로 영구 보관 (본인이 삭제하기 전까지 내 프로필에 유지)

**에러 케이스**
| 케이스 | 에러 코드 | 처리 |
|--------|-----------|------|
| 크레딧 소진 | `ERR-103-A` | 크레딧 충전 안내 바텀시트 |
| AI 생성 실패 | `ERR-103-B` | 자동 재시도 1회 → 실패 시 안내 + 크레딧 미차감 |
| 타임아웃 (30초 초과) | `ERR-103-C` | 자동 재시도 1회 |
| 부적절한 이미지 감지 | `ERR-103-D` | 생성 거부 안내, 크레딧 미차감 |

---

### 3-3. API 엔드포인트 (AI 시착)

#### POST `/tryon/upload`
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
  "image_url": "https://cdn.simul.io/users/photo_abc123.jpg"
}
```

---

#### POST `/tryon/generate`
AI 시착 생성 요청

**Request**
```json
{
  "user_image_url": "https://cdn.simul.io/temp/abc123.jpg",
  "clothing_image_url": "https://cdn.simul.io/items/xyz789.jpg"
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

#### GET `/tryon/status/{job_id}`
생성 상태 실시간 조회 (SSE 스트림)

**Headers**
```
Accept: text/event-stream
```

**Response (Event Stream)**
```text
event: processing
data: {"status": "processing", "estimated_seconds_left": 15}

event: completed
data: {"job_id": "job_abc123", "status": "completed", "result_image_url": "https://cdn.simul.io/results/result123.jpg", "credit_deducted": true}
```

---

#### GET `/tryon/credits`
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

## 4. 개인 옷장

### 4-1. 화면별 UI 컴포넌트

#### SCR-030 개인 옷장 [Must]
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| CategoryTabBar | SegmentedControl | 전체 / 상의 / 하의 / 아우터 / 신발 / 액세서리 |
| SortDropdown | Dropdown | 최근 추가순 / 자주 시착순 |
| ViewToggle | IconButton | 그리드 뷰 ↔ 리스트 뷰 전환 |
| ItemGrid | GridView | 저장된 아이템 카드 (의류 이미지 위주) |
| AddButton | FAB | 우하단 '+' 아이템 추가 버튼 |
| ItemCountBadge | Text | "총 N개" 아이템 수 표시 |
| EmptyState | View | 아이템 없을 때 안내 |

#### SCR-031 아이템 상세 [Must]
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| ItemImage | ImageView | 아이템 이미지 (풀뷰) |
| MetaInfo | View | 카테고리, 메모 (상세 메타정보는 조회 시에만 노출) |
| VisionSearchButton | Button | Google Vision API를 이용하여 유사 이미지 및 출처 링크 반환 (일회성 노출) |
| TryOnButton | Button | "이 옷으로 바로 가상 시착하기" |
| AddToMyClosetButton | Button | (타인 옷장일 시) "내 옷장으로 복사/저장하기" |
| ShopLinkButton | Button | 외부 쇼핑몰 링크 (검색 결과로 파싱되었을 때만 임시 노출) |
| EditButton | IconButton | 아이템 편집 (내 옷장 소유일 때만) |
| DeleteButton | IconButton | 아이템 삭제 (내 옷장 소유일 때만 확인 다이얼로그) |

#### SCR-032 아이템 추가 [Must]
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| ImageUploadArea | TouchableArea | 캡처한 옷 이미지 등 기기에서 단순 업로드 |
| CategoryPicker | Picker | 카테고리 선택 |
| MemoInput | TextInput | 개인 메모 최대 100자 (선택) |
| SaveButton | Button | 저장 (내 옷장에 추가) → FN-201 실행 |

---

### 4-2. 기능 명세

#### FN-201 아이템 추가 [Must]

**설명:** 패션 아이템을 옷장에 저장하고 메타데이터를 등록한다.

**4가지 추가 경로 (옷장이 중앙 허브 역할)**

| 경로 | 트리거 | 처리 |
|------|--------|------|
| 직접 추가 | SCR-032 | 사용자가 직접 캡처본/이미지를 업로드하여 단순 저장 (지연 없는 1차 관문) |
| 타 유저 옷장에서 복사 | SCR-031 (타인 옷장 상세) | 타인의 옷을 보다가 '내 옷장에 추가' 버튼 탭 |
| 시착 시 기본 저장 | FN-103 | 시착 로직 완료되면 무조건 내 옷장에도 보관 |
| 피드 태그에서 저장 | SCR-011 상품 태그 "저장" | 피드에 태그된 옷의 이미지만 내 옷장에 복사 저장 |

**저장 조건**
- 단일 의류 이미지 필수 저장 (업로드 시 무거운 크롤링을 즉시 하지 않고 이미지 앨범 형태로 먼저 저장)
- 카테고리 기입 (선택사항, 미기입 시 미분류 등으로 자동 처리)
- 저장 상한: 계정당 200개 (초과 시 `ERR-201-A`)

**에러 케이스**
| 케이스 | 에러 코드 | 처리 |
|--------|-----------|------|
| 저장 상한 초과 | `ERR-201-A` | "옷장이 가득 찼어요" 안내, 삭제 유도 |
| 이미지 크기 초과 | `ERR-201-B` | 클라이언트 단 차단 (max 10MB) |

---

#### FN-203 아이템 삭제 [Must]

**설명:** 옷장에서 아이템을 삭제한다.

**처리 흐름**
1. 삭제 버튼 탭 → 확인 다이얼로그 ("정말 삭제할까요?")
2. 확인 시 소프트 딜리트 처리 (DB `deleted_at` 세팅)
3. 연결된 시착 결과 이력은 유지 (아이템만 옷장에서 제거)
4. 내가 올린 옷을 다른 유저가 복사해 간 경우, 타 유저의 옷장에 보관된 사본에는 아무런 영향을 주지 않음 (독립 객체 취급)

---

#### FN-204 유사 이미지 및 출처 검색 [Should]

**설명:** 옷장 상세 화면(SCR-031)에서 이미지를 Google Vision API로 검색하여, 유사한 이미지가 포함된 여러 웹사이트 출처 결과를 구글 렌즈(Google Lens) 검색처럼 그대로 UI에 일회성 노출한다.

**처리 흐름**
1. 아이템 상세 화면에서 "유사 기능 검색" 버튼 탭
2. 옷 이미지를 Google Vision API로 전송하여 웹 결과 탐색
3. 반환된 웹사이트 출처 리스트(해당 출처 이미지, 페이지 제목, URL)를 백엔드 필터링 없이 그대로 구성
4. 검색 결과를 리스트 형태로 UI에 즉시 표출 (DB 접근 안 함)

**에러 케이스**
| 케이스 | 에러 코드 | 처리 |
|--------|-----------|------|
| 비슷한 옷 검색 실패 | `ERR-204-A` | "비슷한 제품 출처를 찾지 못했어요" 안내 토스트 |

---

### 4-3. API 엔드포인트 (옷장)

#### POST `/closet/items`
아이템 추가

**Request**
```json
{
  "image_url": "https://cdn.simul.io/items/abc.jpg",
  "category": "top",           // optional (추후 옷장에서 수동 분류 기능)
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

#### GET `/closet/items`
아이템 목록 조회

**Query Parameters**
```
category=top          // optional
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
      "memo": "여름용",
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

#### DELETE `/closet/items/{item_id}`
아이템 삭제

**Response `200`**
```json
{
  "item_id": "item_abc123",
  "deleted_at": "2025-01-01T00:00:00Z"
}
```

---

## 5. 커뮤니티 피드

### 5-1. 화면별 UI 컴포넌트

#### SCR-010 홈 피드 [Must]
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| TopTabBar | SegmentedControl | 전체 / 팔로잉 탭 |
| SortToggle | SegmentedControl | 최신순 / 인기순 (24h 조회수 기준) |
| FeedGrid | GridView | 2열 이미지 그리드, 무한 스크롤 |
| PostCard | View | 시착 결과 썸네일 이미지 |
| FloatingPostButton | FAB | 우하단 게시물 작성 버튼 |

#### SCR-011 게시물 상세 [Must]
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| PostImage | ImageView | 시착 결과 이미지 (오직 1장) |
| TryOnBanner | Button | "이 게시물의 옷 구경하기 (작성자의 옷장으로 이동)" |
| AuthorRow | View | 프로필 이미지 + 닉네임 + 팔로우 버튼 |
| CaptionText | Text | 캡션 (300자, 더보기 접힘) |
| LikeButton | IconButton | 하트 아이콘 + 좋아요 수 |
| CommentSection | View | 댓글 목록 + 입력창 |
| ReportButton | IconButton | 신고 |

#### SCR-012 게시물 작성 [Must]
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| ImageUploadArea | TouchableArea | 시착 결과 이미지 첨부 (오직 1장 필수) |
| CaptionInput | TextArea | 캡션 입력, 300자 카운터 |
| VisibilityToggle | Toggle | 공개 / 비공개 |
| UploadButton | Button | 업로드 (이미지 미첨부 시 비활성화) |

---

### 5-2. 기능 명세

#### FN-301 게시물 작성 [Must]

**입력 조건**
| 필드 | 필수 여부 | 제약 |
|------|-----------|------|
| 이미지 | 필수 | 오직 1장, 최대 20MB, JPG/PNG/HEIC |
| 캡션 | 선택 | 최대 300자 |
| 공개 여부 | 필수 | 기본값: 공개 |

**처리 흐름**
1. 업로드할 시착 결과 이미지 1장 선택
2. 캡션 입력 (선택)
3. 업로드 버튼 탭
4. 이미지 업로드 → 게시물 생성 API 호출
5. 완료 후 피드로 이동

**에러 케이스**
| 케이스 | 에러 코드 | 처리 |
|--------|-----------|------|
| 이미지 미첨부 | `ERR-301-A` | 업로드 버튼 비활성화 |
| 이미지 용량 초과 | `ERR-301-B` | 해당 이미지 선택 차단, 안내 토스트 |
| 업로드 실패 | `ERR-301-C` | 재시도 버튼 노출 |

---

#### FN-304 좋아요 [Should]

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

#### FN-305 댓글 [Could]

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

#### POST `/posts`
게시물 생성

**Request**
```json
{
  "image_url": "https://cdn.simul.io/posts/img1.jpg",
  "caption": "오늘 코디",          // optional, max 300자
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

#### GET `/posts`
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
      "image_url": "https://...",
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

#### POST `/posts/{post_id}/likes`
좋아요 토글

**Response `200`**
```json
{
  "liked": true,
  "like_count": 43
}
```

---

#### GET `/posts/{post_id}/comments`
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

#### POST `/posts/{post_id}/comments`
댓글 작성

**Request**
```json
{
  "content": "어디서 샀어요?",     // max 200자
  "parent_comment_id": null        // 대댓글이면 부모 comment_id
}
```

---

## 7. 계정 & 프로필

### 7-1. 화면별 UI 컴포넌트

#### SCR-003 로그인 [Must]
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| KakaoLoginButton | Button | 카카오 소셜 로그인 |
| NaverLoginButton | Button | 네이버 소셜 로그인 |
| GoogleLoginButton | Button | 구글 소셜 로그인 |
| EmailLoginLink | TextButton | 이메일 로그인 (선택) |
| PrivacyPolicyLink | TextButton | 개인정보처리방침 동의 안내 |

#### SCR-050 프로필 [Must]
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| ProfileHeader | View | 프로필 이미지 + 닉네임 + 한줄 소개 |
| StatsRow | View | 팔로워 / 팔로잉 / 게시물 수 |
| ClosetGridArea | View | 해당 유저의 '옷장' 열람 진입 구역 (피드-옷장 조회 UX) |
| PostGrid | GridView | 해당 유저가 올린 가상 시착 게시물 그리드 |
| ProfileActionBtn | Button | 본인이면 "프로필 편집", 타인이면 "팔로우 기능을 겸하는 액션" |
| SettingButton | IconButton | 설정 (내 프로필 화면일 때만 노출) |

---

### 7-2. API 엔드포인트 (계정)

#### POST `/auth/social`
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

#### PATCH `/users/me`
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

#### GET `/users/{user_id}/posts`
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
├── role             ENUM (user, admin), DEFAULT user
├── is_active        BOOLEAN, DEFAULT true (정지 여부)
├── created_at       TIMESTAMP
└── deleted_at       TIMESTAMP, nullable (탈퇴)
```

### Base Images (사람 이미지 베이스 보관소)
```
base_images
├── base_image_id    UUID, PK
├── user_id          UUID, FK → users
├── image_url        TEXT, NOT NULL
├── source_post_id   UUID, FK → posts, nullable (시착물을 베이스로 재사용한 경우 출처)
├── created_at       TIMESTAMP
└── deleted_at       TIMESTAMP, nullable
```

### Posts (시착 결과 통합 보관소)
```
posts
├── post_id          UUID, PK
├── user_id          UUID, FK → users
├── base_image_id    UUID, FK → base_images (시착에 사용한 사람 사진 출처)
├── item_id          UUID, FK → closet_items, nullable (시착 옷 출처)
├── image_url        TEXT, nullable (시착 완료 전 null)
├── status           ENUM (processing, completed, failed)
├── caption          VARCHAR(300), nullable
├── is_public        BOOLEAN, DEFAULT false (기본 비공개)
├── like_count       INT, DEFAULT 0
├── view_count       INT, DEFAULT 0
├── created_at       TIMESTAMP
└── deleted_at       TIMESTAMP, nullable
```



### Clothing Images (원본 옷 이미지)
```
clothing_images
├── image_id         UUID, PK
├── image_url        TEXT, NOT NULL
├── uploader_id      UUID, FK → users (최초 업로더)
└── created_at       TIMESTAMP
```

### Closet Items (사용자 옷장 매핑)
```
closet_items
├── item_id          UUID, PK
├── user_id          UUID, FK → users
├── image_id         UUID, FK → clothing_images
├── category         ENUM (top, bottom, outer, shoes, accessory), nullable
├── memo             VARCHAR(100)
├── try_count        INT, DEFAULT 0
├── created_at       TIMESTAMP
└── deleted_at       TIMESTAMP, nullable
```

### Tryon Credits
```
tryon_credits
├── credit_id        UUID, PK
├── user_id          UUID, FK → users
├── used_at          TIMESTAMP
└── job_id           UUID, FK → posts
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
| POST | `/auth/social` | 소셜 로그인 | 불필요 |
| POST | `/auth/refresh` | 토큰 갱신 | 불필요 |
| DELETE | `/auth/logout` | 로그아웃 | 필요 |
| GET | `/users/me` | 내 정보 조회 | 필요 |
| PATCH | `/users/me` | 프로필 수정 | 필요 |
| DELETE | `/users/me` | 회원 탈퇴 | 필요 |
| GET | `/users/{user_id}` | 사용자 프로필 조회 | 불필요 |
| GET | `/users/{user_id}/posts` | 사용자 게시물 목록 | 불필요 |
| POST | `/follows/{user_id}` | 팔로우 | 필요 |
| DELETE | `/follows/{user_id}` | 언팔로우 | 필요 |
| GET | `/posts` | 피드 목록 | 불필요 (비회원 열람) |
| POST | `/posts` | 게시물 작성 | 필요 |
| GET | `/posts/{post_id}` | 게시물 상세 | 불필요 |
| DELETE | `/posts/{post_id}` | 게시물 삭제 | 필요 |
| POST | `/posts/{post_id}/likes` | 좋아요 토글 | 필요 |
| GET | `/posts/{post_id}/comments` | 댓글 목록 | 불필요 |
| POST | `/posts/{post_id}/comments` | 댓글 작성 | 필요 |
| DELETE | `/comments/{comment_id}` | 댓글 삭제 | 필요 |
| POST | `/posts/{post_id}/report` | 게시물 신고 | 필요 |
| GET | `/closet/items` | 옷장 아이템 목록 | 필요 |
| POST | `/closet/items` | 아이템 추가 | 필요 |
| GET | `/closet/items/{item_id}` | 아이템 상세 | 필요 |
| PATCH | `/closet/items/{item_id}` | 아이템 수정 | 필요 |
| DELETE | `/closet/items/{item_id}` | 아이템 삭제 | 필요 |
| POST | `/tryon/upload` | 시착 사진 업로드 | 필요 |
| POST | `/tryon/generate` | 시착 생성 요청 | 필요 |
| GET | `/tryon/status/{job_id}` | 생성 상태 조회 (SSE) | 필요 |
| GET | `/tryon/credits` | 크레딧 잔여 조회 | 필요 |
| GET | `/admin/reports` | 접수된 신고 목록 조회 | Admin |
| PATCH | `/admin/posts/{post_id}/blind` | 문제 게시글 강제 블라인드 | Admin |
| PATCH | `/admin/users/{user_id}/suspend` | 악성 유저 정지 처리 | Admin |
| POST | `/admin/users/{user_id}/credits` | 크레딧 수동 지급 | Admin |

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
| `ERR-204-A` | 200 | 유사 이미지 출처 없음 | "비슷한 제품 출처를 찾지 못했어요" |
| `ERR-301-A` | 422 | 이미지 미첨부 업로드 시도 | 업로드 버튼 비활성화 |
| `ERR-301-B` | 422 | 게시물 이미지 크기 초과 | "이미지는 20MB 이하만 가능해요" |
| `ERR-301-C` | 500 | 게시물 업로드 실패 | "업로드에 실패했어요. 다시 시도해주세요" |
| `ERR-304-A` | 401 | 비로그인 좋아요 시도 | 로그인 유도 바텀시트 |
| `ERR-304-B` | 500 | 좋아요 네트워크 오류 | UI 롤백 + "잠시 후 다시 시도해주세요" |
| `ERR-305-A` | 401 | 비로그인 댓글 시도 | 로그인 유도 바텀시트 |
| `ERR-305-B` | 422 | 댓글 200자 초과 | 입력 차단 + 카운터 빨간색 표시 |

### 공통 예외 처리 원칙

1. **낙관적 업데이트**: 좋아요 등 빠른 인터랙션은 UI 먼저 반영 후 API 실패 시 롤백
2. **비로그인 접근**: 피드/상세 열람은 허용, 시착/저장/공유/좋아요/댓글은 로그인 유도 바텀시트
3. **네트워크 없음**: 전역 오프라인 배너 표시, 캐시된 데이터 표시 (읽기 전용)
4. **토큰 만료**: 자동 토큰 갱신 시도 → 실패 시 로그인 화면으로 이동
5. **서버 점검**: 503 응답 시 점검 안내 화면 표시
