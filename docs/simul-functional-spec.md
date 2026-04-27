# Simul 기능 명세서

> 대상 독자: 프론트엔드 / 백엔드 개발자  
> 기준 문서: Simul MVP 요구사항 기능서 v1.0  
> 플랫폼: 반응형 웹 (Web, 모바일 최적화)

---

## 목차

1. [문서 규칙](#1-문서-규칙)
2. [화면 ID / 기능 ID 체계](#2-화면-id--기능-id-체계)
3. [AI 가상시착](#3-ai-가상시착)
4. [개인 옷장](#4-개인-옷장)
5. [커뮤니티 피드](#5-커뮤니티-피드)
6. [관리자 (Admin)](#6-관리자-admin)
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
| SCR-070 | 통합 검색 | 홈 피드 상단 검색바 |
| SCR-080 | 알림 | 헤더 알림 버튼 |

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
| FN-307 | 자동 태그 (Google Vision) | SCR-012, SCR-024 |
| FN-308 | 통합 검색 | SCR-070 |
| FN-501 | 소셜 로그인 | SCR-003 |
| FN-502 | 프로필 편집 | SCR-051 |
| FN-503 | 알림 설정 | SCR-052 |
| FN-601 | 알림 | SCR-080 |
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
| ProgressBar | View | 상단 진행률 표시 + 처리 단계 안내 텍스트 |
| RandomFeedSwiper | SwipeView | 랜덤 인기 피드 세로 풀스크린 스와이프 (숏폼 형태) |
| FeedLikeButton | IconButton | 현재 보고 있는 피드에 좋아요 (기존 좋아요 API 재사용) |
| FeedAuthorInfo | View | 작성자 프로필 미니 표시 (닉네임 + 프로필 사진) |
| CompletionBanner | OverlayView | 생성 완료 시 "시착 완료! 결과 보기" 오버레이 배너 자동 등장 |
| CancelButton | Button | 시착 취소 (크레딧 미차감) |

#### SCR-024 시착 결과 [Must]
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| CompareSlider | SwipeView | 원본 ↔ 결과 비교 슬라이더 |
| SaveButton | Button | 디바이스 저장 |
| CreatePostButton | Button | `[Must]` "게시물 만들기" — SCR-012 게시물 생성 화면으로 이동, 시착 결과 이미지가 첫 번째 이미지로 자동 완성됨 |
| RetryButton | Button | 다른 옷으로 다시 시착 |

*Note: 시착한 모든 이미지는 DB에 자동 저장됩니다. 게시물 생성 화면(SCR-012)에서 서버에 저장된 시착 결과를 추가로 선택할 수 있습니다.*

---

### 3-2. 기능 명세

#### FN-101 사람 이미지(베이스) 선택 및 등록 [Must]

**설명:** 사용자의 본인 사진, 혹은 이전에 잘 나온 AI 시착결과물을 시착에 사용할 베이스 모델로 등록/선택한다.

**입력 조건**
- 이미지 포맷: JPG, PNG, HEIC
- 최소 해상도: 256 × 256px
- 최대 파일 크기: 20MB

**처리 흐름**
1. 저장된 나의 베이스 이미지 목록 조회 후 선택
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
4. 대기 화면(SCR-023)에서 랜덤 인기 피드를 숏폼 스와이프로 노출하여 사용자 대기 이탈 방지 (기존 `GET /posts?sort=popular` 재사용)
5. 완료 수신 시 결과 이미지를 내 프로필의 "비공개 게시물(is_public=false)"로 자동 생성 후 SCR-024 이동
6. 크레딧 1 차감 (생성 성공 시에만)
7. 생성된 시착 게시글에 사용된 의류 출처(item_id)를 연결
8. 사용자 원본 사진은 삭제하지 않고 유지하여 다음 시착 시 재사용

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

#### POST `/tryon/base-images`
사용자 사진(베이스 이미지) 업로드 및 등록

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
  "base_image_id": "uuid",
  "image_url": "https://cdn.simul.io/users/photo_abc123.jpg"
}
```

---

#### POST `/tryon/generate`
AI 시착 생성 요청

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
  "job_id": "post_uuid",
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
| SearchBar | TextInput | `[Must]` 통합 검색바 — # 입력 시 태그 자동완성 드롭다운 노출 (SCR-070 진입) |
| TopTabBar | SegmentedControl | 전체 / 팔로잉 탭 |
| SortToggle | SegmentedControl | 최신순 / 인기순 (24h 조회수 기준) |
| FeedGrid | GridView | 2열 이미지 그리드, 무한 스크롤 |
| PostCard | View | 시착 결과 썸네일 이미지 + 태그 칩 (최대 3개 노출) |
| FloatingPostButton | FAB | 우하단 게시물 작성 버튼 |

#### SCR-011 게시물 상세 [Must]
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| PostImageCarousel | SwipeView | 게시물 이미지 (다중 이미지 지원, 좌우 스와이프) |
| TagChipList | ChipGroup | 게시물에 부착된 태그 칩 목록 (탭 시 해당 태그 검색 결과로 이동) |
| TryOnBanner | Button | "이 게시물의 옷 구경하기 (작성자의 옷장으로 이동)" |
| AuthorRow | View | 프로필 이미지 + 닉네임 + 팔로우 버튼 |
| CaptionText | Text | 캡션 (300자, 더보기 접힘) |
| LikeButton | IconButton | 하트 아이콘 + 좋아요 수 |
| CommentSection | View | 댓글 목록 + 입력창 |
| ReportButton | IconButton | 신고 |

#### SCR-012 게시물 작성 [Must]
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| ImageUploadArea | TouchableArea | 이미지 첨부 (최소 1장 필수, 최대 5장 다중 선택 가능 — **로컬 앨범** 또는 **서버에 저장된 시착 결과**에서 선택) |
| AutoFilledImage | ImageView | 시착 직후 진입 시 시착 결과 이미지가 첫 번째로 자동 완성됨 (추가 이미지 선택 가능) |
| ImageOrderControl | DragList | 업로드된 이미지 순서 변경 (드래그 앤 드롭) |
| TagAutoComplete | TextInput + ChipGroup | `[Must]` Google Vision API 자동 태그 추천 → 자동완성 목록 → 사용자가 +/- 편집 (최대 10개) |
| CaptionInput | TextArea | 캡션 입력, 300자 카운터 |
| VisibilityToggle | Toggle | 공개 / 비공개 |
| UploadButton | Button | 업로드 (이미지 미첨부 시 비활성화) |

*Note: 시착 직후 SCR-024 → SCR-012 진입 시, 시착 결과 이미지가 첫 번째 슬롯에 자동 완성됩니다. 사용자는 추가로 로컬 앨범이나 서버에 저장된 다른 시착 결과를 이미지로 추가할 수 있습니다.*

#### SCR-070 통합 검색 [Must]
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| SearchInput | TextInput | 검색어 입력 — # 입력 시 태그 모드 전환 |
| TagSuggestionDropdown | ListView | # 입력 후 태그 자동완성 드롭다운 (카테고리별 그룹핑: 스타일, 상의, 하의, 아우터 등) |
| SearchResultGrid | GridView | 검색 결과 게시물 그리드 (태그 매칭 + 캡션 매칭) |
| PopularTagSection | ChipGroup | 인기 태그 칩 목록 (검색어 미입력 시 기본 노출) |
| RecentSearchList | ListView | 최근 검색어 목록 |
| NoResultState | View | 검색 결과 없을 때 안내 |

---

### 5-2. 기능 명세

#### FN-301 게시물 작성 [Must]

**입력 조건**
| 필드 | 필수 여부 | 제약 |
|------|-----------|------|
| 이미지 | 필수 | 최소 1장, **최대 5장** (다중 선택 가능), 각 이미지 최대 20MB, JPG/PNG/HEIC |
| 태그 | 선택 | 최대 10개 (Google Vision API 자동 추출 후 사용자 편집) |
| 캡션 | 선택 | 최대 300자 |
| 공개 여부 | 필수 | 기본값: 공개 |

**게시물 작성 경로 (2가지)**

| 경로 | 트리거 | 처리 |
|------|--------|------|
| 시착 직후 게시물 생성 | SCR-024 CreatePostButton | 시착 결과 이미지가 **첫 번째 이미지로 자동 완성**된 게시물 생성 화면(SCR-012)으로 이동. 추가 이미지 선택 가능 |
| 일반 게시물 작성 | SCR-012 직접 진입 | **로컬 앨범** 또는 **서버에 저장된 시착 결과** 이미지에서 여러 장을 선택하여 게시 |

*Note: 시착 시 생성되는 모든 이미지는 DB에 영구 저장됩니다 (Redis 캐시를 사용하지 않습니다).*

**처리 흐름**
1. 시착 직후: SCR-024에서 "게시물 만들기" 버튼 탭 → SCR-012로 이동 (시착 결과 이미지가 첫 번째 슬롯에 자동 완성)
2. 일반 진입: SCR-012에서 이미지 선택 (로컬 앨범 또는 서버에 저장된 시착 결과에서 다중 선택, 최대 5장)
3. 이미지 업로드 완료 후 Google Vision API로 첫 번째 이미지를 분석하여 옷 관련 키워드 자동 추출 → 자동완성 태그 추천
4. 사용자가 추천된 태그에서 +/- 편집 (최대 10개)
5. 캡션 입력 (선택)
6. 업로드 버튼 탭
7. 이미지 업로드 → 게시물 생성 API 호출 (다중 이미지 + 태그 포함)
8. 완료 후 피드로 이동

**에러 케이스**
| 케이스 | 에러 코드 | 처리 |
|--------|-----------|------|
| 이미지 미첨부 | `ERR-301-A` | 업로드 버튼 비활성화 |
| 이미지 용량 초과 | `ERR-301-B` | 해당 이미지 선택 차단, 안내 토스트 |
| 이미지 5장 초과 | `ERR-301-D` | "이미지는 최대 5장까지 첨부할 수 있어요" 안내 토스트 |
| 업로드 실패 | `ERR-301-C` | 재시도 버튼 노출 |
| 태그 10개 초과 | `ERR-307-A` | 추가 차단, "태그는 최대 10개까지 가능해요" 안내 |

---

#### FN-307 자동 태그 (Google Vision API) [Must]

**설명:** 이미지 업로드 시 Google Vision API를 활용하여 옷 관련 키워드(예: 데님, 니트, 자켓 등)를 자동 추출하고, 태그로 추천한다. 사용자 부담을 최소화하기 위해 자동 완성 형태로 제공된다.

**태그 가시성 흐름:**
1. 가상 시착 완료 후 (SCR-024) 또는 로컬 앨범에서 이미지 선택 및 업로드 (SCR-012)
2. Google Vision API를 이용해 이미지 분석 → 옷 관련 라벨/키워드 추출
3. 추출된 키워드를 자동완성 태그로 추천 표시
4. 사용자가 취향에 따라 태그를 추가(+) 또는 제거(-) (최대 10개)
5. 확정된 태그는 게시물에 부착되어 피드에서 노출됨

**태그 카테고리 분류:**
| 카테고리 | 태그 예시 |
|----------|----------|
| 스타일 | 캐주얼, 포멀, 스트릿, 미니멀 |
| 상의 | 티셔츠, 니트, 블라우스, 셔츠 |
| 하의 | 데님, 슬랙스, 스커트, 쇼츠 |
| 아우터 | 자켓, 코트, 가디건, 패딩 |
| 소재/패턴 | 체크, 스트라이프, 레더, 플로럴 |

**제약**
- 게시물당 태그 최대 10개
- 태그명 최대 20자
- 태그는 게시물 상세 및 피드 카드에 항상 노출

**에러 케이스**
| 케이스 | 에러 코드 | 처리 |
|--------|-----------|------|
| Vision API 분석 실패 | `ERR-307-B` | 자동 태그 없이 수동 입력만 허용, 안내 토스트 |
| 태그 10개 초과 | `ERR-307-A` | 추가 버튼 비활성화 |

---

#### FN-308 통합 검색 [Must]

**설명:** 피드 상단 검색바를 통해 게시물을 통합 검색한다. `#`을 입력하면 태그 검색 모드로 전환되어 카테고리별 태그 미리보기를 표시한다.

**처리 흐름**
1. 홈 피드 상단 검색바 탭 → SCR-070 통합 검색 진입
2. 검색어 미입력 시: 인기 태그 칩 목록 + 최근 검색어 표시
3. 일반 텍스트 입력 시: 캡션 + 닉네임 통합 검색 (디바운스 300ms)
4. `#` 입력 시: 태그 검색 모드 전환
   - 입력 중인 텍스트로 태그 자동완성 드롭다운 노출
   - 카테고리별 그룹핑 (스타일/상의/하의/아우터/소재 등)
   - 예: `#데님` 입력 → "데님" 태그 포함 게시물 미리보기 하단 표시
5. 태그 선택 시: 해당 태그가 부착된 게시물 그리드 표시

**제약**
- 검색어 최소 1자 이상
- 태그 자동완성은 usage_count 기준 인기순 정렬
- 검색 결과 페이지네이션 지원 (page/per_page)

**에러 케이스**
| 케이스 | 에러 코드 | 처리 |
|--------|-----------|------|
| 검색 결과 없음 | — | "검색 결과가 없어요" 안내 + 인기 태그 추천 |

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
게시물 생성 (다중 이미지 + 태그 지원)

**Request**
```json
{
  "image_urls": [
    "https://cdn.simul.io/posts/img1.jpg",
    "https://cdn.simul.io/posts/img2.jpg"
  ],
  "tags": ["데님", "캐주얼", "스트릿"],  // optional, max 10개
  "caption": "오늘 코디",                  // optional, max 300자
  "is_public": true
}
```
*Note: `image_urls`는 최소 1개, 최대 5개. 첫 번째 이미지가 대표 이미지(`posts.image_url`)로 저장됩니다.*

**Response `201`**
```json
{
  "post_id": "post_abc123",
  "tags": ["데님", "캐주얼", "스트릿"],
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
tag=데님            // optional, 태그 필터
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
      "image_count": 3,
      "tags": ["데님", "캐주얼"],
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

#### POST `/tags/analyze`
Google Vision API 이미지 분석 → 태그 자동 추출

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

---

#### GET `/tags/search`
태그 자동완성 검색 (# 입력 시)

**Query Parameters**
```
q=데님              // 검색 키워드
limit=10            // 최대 반환 수
```

**Response `200`**
```json
{
  "tags": [
    { "tag_id": "uuid", "name": "데님", "category": "하의", "usage_count": 1520 },
    { "tag_id": "uuid", "name": "데님자켓", "category": "아우터", "usage_count": 340 }
  ]
}
```

---

#### GET `/search`
통합 검색

**Query Parameters**
```
q=데님              // 검색 키워드
type=tag            // tag | caption | all (기본값: all)
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
      "tags": ["데님", "캐주얼"],
      "like_count": 42,
      "author": { "user_id": "user_xyz", "nickname": "패션러버" },
      "created_at": "2025-01-01T00:00:00Z"
    }
  ],
  "related_tags": ["데님자켓", "데님셔츠"],
  "total": 50,
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

## 7a. 알림 (Notifications)

### 7a-1. 화면별 UI 컴포넌트

#### SCR-080 알림 [Must]
| 컴포넌트 | 타입 | 설명 |
|----------|------|------|
| NotificationList | ListView | 알림 목록 (시간순 정렬) |
| NotificationItem | View | 알림 카드 — 알림 유형 아이콘 + 메시지 + 시간 + 미읽음 인디케이터 |
| NotificationBadge | Badge | 헤더 알림 버튼에 미읽음 수 표시 |
| EmptyState | View | 알림 없을 때 안내 문구 |
| ReadAllButton | Button | 전체 읽음 처리 |

### 7a-2. 기능 명세

#### FN-601 알림 [Must]

**알림 유형 (4가지)**

| 알림 유형 | 트리거 | 수신자 | 메시지 예시 | 탭 시 이동 |
|-----------|--------|---------|------------|----------|
| `TRYON_COMPLETE` | AI 시착 생성 완료 | 시착 요청자 | "시착이 완료되었어요! 결과를 확인해보세요" | SCR-024 시착 결과 |
| `LIKE` | 내 게시물에 좋아요 | 게시물 작성자 | "{nickname}님이 게시물을 좋아해요" | SCR-011 게시물 상세 |
| `COMMENT` | 내 게시물에 댓글 | 게시물 작성자 | "{nickname}님이 댓글을 남겼어요: {content}" | SCR-011 게시물 상세 |
| `FOLLOW_POST` | 팔로우한 사람이 새 게시물 공개 | 팔로워 | "{nickname}님이 새 게시물을 올렸어요" | SCR-011 게시물 상세 |

**알림 생성 흐름**
1. **TRYON_COMPLETE**: AI 시착 생성 완료(status=completed) 시, TryOn 도메인이 알림 생성 요청
2. **LIKE**: 좋아요 토글 시, Feed 도메인이 게시물 작성자에게 알림 생성 (본인 좋아요는 제외)
3. **COMMENT**: 댓글 작성 시, Feed 도메인이 게시물 작성자에게 알림 생성 (본인 댓글은 제외)
4. **FOLLOW_POST**: 게시물이 공개(`is_public=true`)로 전환될 때, Feed 도메인이 작성자의 팔로워 전체에게 알림 생성

**알림 조회 및 읽음 처리:**
1. 헤더 알림 버튼에 미읽음 알림 수 배지 표시
2. 알림 페이지(SCR-080) 진입 시 목록 조회 (unread 우선 정렬)
3. 개별 알림 탭 시: 해당 알림 읽음 처리 + 관련 화면으로 이동
4. "전체 읽음" 버튼으로 일괄 읽음 처리 가능

**제약**
- 알림 대상: 로그인된 사용자만
- 본인 활동(좋아요/댓글)에 대한 알림은 생성하지 않음
- 알림 보관 기간: 30일 (이후 자동 삭제)
- 알림 목록 페이지네이션 지원 (page/per_page)

---

### 7a-3. API 엔드포인트 (알림)

#### GET `/notifications`
알림 목록 조회

**Query Parameters**
```
page=1
per_page=20
```

**Response `200`**
```json
{
  "notifications": [
    {
      "notification_id": "uuid",
      "type": "LIKE",
      "actor": {
        "user_id": "uuid",
        "nickname": "패션러버",
        "profile_image_url": "https://..."
      },
      "reference_id": "post_uuid",
      "message": "패션러버님이 게시물을 좋아해요",
      "is_read": false,
      "created_at": "ISO8601"
    }
  ],
  "unread_count": 5,
  "total": 42,
  "page": 1,
  "per_page": 20
}
```

---

#### GET `/notifications/unread-count`
미읽음 알림 수 조회 (헤더 배지 용)

**Response `200`**
```json
{
  "unread_count": 5
}
```

---

#### PATCH `/notifications/{notification_id}/read`
개별 알림 읽음 처리

**Response `200 OK`**

---

#### PATCH `/notifications/read-all`
전체 알림 일괄 읽음 처리

**Response `200 OK`**

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
├── image_url        TEXT, nullable (대표 이미지, 첫 번째 이미지)
├── status           ENUM (processing, completed, failed)
├── caption          VARCHAR(300), nullable
├── is_public        BOOLEAN, DEFAULT false (기본 비공개)
├── is_blinded       BOOLEAN, DEFAULT false (신고 누적 자동 블라인드)
├── report_count     INT, DEFAULT 0
├── like_count       INT, DEFAULT 0
├── view_count       INT, DEFAULT 0
├── created_at       TIMESTAMP
└── deleted_at       TIMESTAMP, nullable
```

### Post Images (게시물 다중 이미지)
```
post_images
├── post_image_id    UUID, PK
├── post_id          UUID, FK → posts
├── image_url        TEXT, NOT NULL
├── sort_order       INT, DEFAULT 0 (정렬 순서)
└── created_at       TIMESTAMP
```

### Tags (태그 마스터)
```
tags
├── tag_id           UUID, PK
├── name             VARCHAR(20), UNIQUE, NOT NULL (태그명)
├── category         VARCHAR(20), nullable (스타일/상의/하의/아우터/소재 등)
├── usage_count      INT, DEFAULT 0 (사용 빈도, 검색 자동완성 정렬용)
└── created_at       TIMESTAMP
```

### Post Tags (게시물-태그 매핑)
```
post_tags
├── post_tag_id      UUID, PK
├── post_id          UUID, FK → posts
├── tag_id           UUID, FK → tags
├── created_at       TIMESTAMP
└── UNIQUE (post_id, tag_id)
```

### Notifications (알림)
```
notifications
├── notification_id  UUID, PK
├── recipient_id     UUID, FK → users (알림 수신자)
├── actor_id         UUID, FK → users, nullable (알림 발생자)
├── type             ENUM (TRYON_COMPLETE, LIKE, COMMENT, FOLLOW_POST)
├── reference_id     UUID, nullable (관련 리소스 ID — post_id 등)
├── message          VARCHAR(200)
├── is_read          BOOLEAN, DEFAULT false
└── created_at       TIMESTAMP
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

### Likes
```
likes
├── like_id          UUID, PK
├── post_id          UUID, FK → posts
├── user_id          UUID, FK → users
├── created_at       TIMESTAMP
└── UNIQUE (post_id, user_id)
```

### Reports
```
reports
├── report_id        UUID, PK
├── post_id          UUID, FK → posts
├── reporter_id      UUID, FK → users
├── reason           VARCHAR(200)
├── created_at       TIMESTAMP
└── UNIQUE (post_id, reporter_id)
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
| POST | `/tags/analyze` | 이미지 태그 자동 추출 (Vision API) | 필요 |
| GET | `/tags/search` | 태그 자동완성 검색 | 불필요 |
| GET | `/search` | 통합 검색 | 불필요 |
| POST | `/tryon/base-images` | 새 베이스 이미지 업로드 등록 | 필요 |
| POST | `/tryon/base-images/from-post` | 시착 결과를 베이스로 복제 등록 | 필요 |
| GET | `/users/me/base-images` | 내 베이스 이미지 목록 조회 | 필요 |
| POST | `/tryon/generate` | 시착 생성 요청 | 필요 |
| GET | `/tryon/status/{job_id}` | 생성 상태 조회 (SSE) | 필요 |
| GET | `/tryon/credits` | 크레딧 잔여 조회 | 필요 |
| GET | `/admin/reports` | 접수된 신고 목록 조회 | Admin |
| PATCH | `/admin/posts/{post_id}/blind` | 문제 게시글 강제 블라인드 | Admin |
| PATCH | `/admin/posts/{post_id}/unblind` | 블라인드 해제 (복구) | Admin |
| PATCH | `/admin/users/{user_id}/suspend` | 악성 유저 정지 처리 | Admin |
| POST | `/admin/users/{user_id}/credits` | 크레딧 수동 지급 | Admin |
| GET | `/notifications` | 알림 목록 조회 | 필요 |
| GET | `/notifications/unread-count` | 미읽음 알림 수 조회 | 필요 |
| PATCH | `/notifications/{notification_id}/read` | 개별 알림 읽음 처리 | 필요 |
| PATCH | `/notifications/read-all` | 전체 알림 일괄 읽음 처리 | 필요 |

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
| `ERR-301-D` | 422 | 게시물 이미지 5장 초과 | "이미지는 최대 5장까지 첨부할 수 있어요" |
| `ERR-304-A` | 401 | 비로그인 좋아요 시도 | 로그인 유도 바텀시트 |
| `ERR-304-B` | 500 | 좋아요 네트워크 오류 | UI 롤백 + "잠시 후 다시 시도해주세요" |
| `ERR-305-A` | 401 | 비로그인 댓글 시도 | 로그인 유도 바텀시트 |
| `ERR-305-B` | 422 | 댓글 200자 초과 | 입력 차단 + 카운터 빨간색 표시 |
| `ERR-307-A` | 422 | 태그 10개 초과 | 추가 버튼 비활성화 + "태그는 최대 10개까지 가능해요" |
| `ERR-307-B` | 500 | Vision API 태그 분석 실패 | "자동 태그 추출에 실패했어요. 수동으로 입력해주세요" |
| `ERR-401-A` | 422 | 동일 게시물 중복 신고 | "이미 신고한 게시물이에요" |

### 공통 예외 처리 원칙

1. **낙관적 업데이트**: 좋아요 등 빠른 인터랙션은 UI 먼저 반영 후 API 실패 시 롤백
2. **비로그인 접근**: 피드/상세 열람은 허용, 시착/저장/공유/좋아요/댓글은 로그인 유도 바텀시트
3. **네트워크 없음**: 전역 오프라인 배너 표시, 캐시된 데이터 표시 (읽기 전용)
4. **토큰 만료**: 자동 토큰 갱신 시도 → 실패 시 로그인 화면으로 이동
5. **서버 점검**: 503 응답 시 점검 안내 화면 표시
