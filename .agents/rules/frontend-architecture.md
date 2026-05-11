---
trigger: always_on
---

# 프론트엔드 규칙 —SIMUL Next.js

## 디자인 규칙

### 1. 브랜드 컬러 시스템
- 모든 컴포넌트에서 하드코딩된 색상값 사용 금지. 반드시 `globals.css`의 전역 CSS 변수를 사용할 것
- **주요 브랜드 변수:**
  | 변수명 | 값 | 용도 |
  |---|---|---|
  | `--color-primary` | `#95b358` | 주요 액센트, 활성 상태 |
  | `--color-green` | `#d1dc55` | 버튼 기본 배경, 글래스모피즘 mix색상 |
  | `--color-green-hover` | `#4a6724` | hover/active 상태 |
- 버튼 `primary` variant의 배경은 `var(--color-green)` 사용

### 2. 간격(Spacing) 규칙
- **콘텐츠 요소 간 기본 갭: `10px`** — 이미지 프레임, 버튼 행, 카드 사이 등 대부분의 인라인 요소 간격에 적용
- **피드 그리드(FeedGrid) 갭: `4px`** — 핀터레스트 스타일의 밀도 높은 레이아웃
- **프로필 그리드 갭: `10px`** — 통일성 있는 모던 둥근 프레임 그리드
- **페이지 좌우 패딩: `16px`** (`--space-page-x`)
- **페이지 상하 패딩: `16px`** (`--space-page-y`)

### 3. 레이아웃 구조
- **Flexbox 우선**: 페이지 높이는 `min-height: calc(100vh - X)` 대신 부모 `display: flex; flex-direction: column` + 자식 `flex: 1` 패턴 사용
- **앱 컨테이너**: `app/layout.module.css`의 `.appContainer`가 `height: 100vh`, `.main`이 `display: flex; flex-direction: column; flex: 1`로 설정되어 있으므로, 각 페이지는 `flex: 1`만 선언하면 자동으로 남은 화면을 채움
- **모바일 퍼스트**: `max-width: 450px` 기준으로 중앙 정렬

### 4. Border Radius 규칙
- **컴포넌트 프레임 외곽**: `20px` (`--radius-comfortable`) — 이미지 프레임, 카드, 입력창 등
- **프로필 페이지 외곽 프레임**: `30px` (아치형 상단)
- **버튼**: `99px` (`--radius-button`) — 완전한 pill/capsule 형태
- **원형 요소(아바타 등)**: `50%` (`--radius-circle`)

### 5. 글래스모피즘(Glassmorphism) 패턴
- SIMUL 프로젝트의 핵심 시각 아이덴티티. Toggle, 페이지네이션 등 UI 컨트롤에 공통 적용
- **기본 레시피:**
  ```css
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.5),
    inset 0 -1px 0 rgba(255, 255, 255, 0.1),
    inset 0 0 28px 14px rgba(255, 255, 255, 1);
  ```
- **활성(Active) 상태:**
  ```css
  background: color-mix(in srgb, var(--color-green) 60%, transparent);
  box-shadow:
    inset 0 0 28px 14px color-mix(in srgb, var(--color-green) 20%, transparent);
  ```

### 6. 헤더 스타일 통일
- 페이지 헤더는 `<header>` 태그로 감싸고 아래 스타일 준수:
  ```css
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 65px; /* Header 높이에 맞춤 */
  z-index: 90;
  ```
- 타이틀 폰트: `20px`, `font-weight: 700`, `font-family: var(--font-family-latin)`

### 7. 이미지 프레임 규칙
- **세로형 이미지 비율: `4:6`** (aspect-ratio) — 의류 시착 결과, 프로필 게시물 등
- **정사각형 그리드: `1:1`** — 프로필 페이지 게시물/옷장 탭
- **이미지는 항상 `object-fit: cover`** + 부모에 `overflow: hidden`
- **가로 스크롤 캐러셀**: `display: flex; gap: 10px; overflow-x: auto;` + 스크롤바 숨김 처리

### 8. 프로필 페이지 레이아웃
- **몰입형 히어로(Immersive Hero)**: 배경 이미지가 상단 45vh를 채우고, 하단으로 갈수록 다크 그라데이션 오버레이 적용
- **다크 테마**: 프로필 페이지 하단(탭 + 그리드)은 `#111` 배경의 다크 모드
- **아치형 프레임**: 전체 컨텐츠를 `border-radius: 30px 30px 0 0`의 프레임으로 감싸며, 외부 컨테이너와 `16px` 패딩
- **탭 전환**: 게시물 ↔ 옷장 두 탭을 `useState`로 토글, 하단 `border-bottom` 활성 표시

### 9. 페이지네이션 및 토글 스타일
- 기본 형태: 완벽한 pill(알약) 형태 유지를 위해 외부 트랙과 내부 버튼 모두 `var(--radius-button)`(99px) 적용
- 활성 페이지/버튼: `--color-green` 기반의 `color-mix` 배경을 가진 글래스모피즘 요소
- 버튼 사이즈: 최소 너비 `34px`, 높이 `42px`

### 10. 게시물 작성(Post Create) 페이지
- **이미지 업로드 영역**: 4:6 비율 프레임의 가로 스크롤 캐러셀 (갭 10px)
- **추가 버튼 프레임**: 마지막 칸은 `--color-surface-sand` 배경 + 중앙 `+` 아이콘
- **캡션 입력창**: 글자 수 카운터(`0/300`)를 textarea 내부 우측 하단에 `position: absolute`로 배치. `pointer-events: none` 적용

### 11. 모바일 뷰포트(Viewport) 최적화
- **앱 감성 UI**: Next.js 14+ 기준 `app/layout.tsx`에 반드시 명시적인 Viewport 설정을 추가할 것
- **필수 설정**:
  ```typescript
  export const viewport: Viewport = {
    width: 'device-width',
    initialScale: 1,
    maximumScale: 1,
    userScalable: false,
    viewportFit: 'cover',
  };
  ```
- 줌(Zoom)을 방지하여 모바일 앱과 같은 조작감을 유지하고, 노치 디자인(viewportFit: 'cover')에 완벽히 대응해야 함

---

## 기술 스택
- Next.js 16+ (App Router), React 19, TypeScript
- Vanilla CSS Module (컴포넌트별 스코프 CSS)
- Zustand (전역 상태: auth, UI)
- iron-session (서버 사이드 JWT 관리)
- BFF 패턴: Next.js API Routes → Spring Boot

## 레이아웃 아키텍처
- **Root Layout 1개** — `app/layout.tsx`: Header + `<main>` + Footer + Toast
- **Header** — `usePathname()`으로 role 자동 감지 (기본/어드민)
- **Footer** — `/admin` 경로에서 숨김
- **Sidebar** — page.tsx에서 직접 import, Route Group/중첩 Layout 없음

## 폴더 구조 원칙

### 페이지 구조
```
app/some-page/
├── page.tsx                  # 150줄 이하! 레이아웃 + 조합만
├── page.module.css
└── _components/              # 페이지 전용 컴포넌트 (동시 생성 필수!)
    ├── SomeSection/
    │   ├── SomeSection.tsx
    │   ├── SomeSection.module.css
    │   └── index.ts
    └── AnotherSection/
```

### 컴포넌트 배치 원칙 (필수)

> **모든 컴포넌트는 사용하는 `page.tsx`와 같은 디렉토리의 `_components/` 안에 위치해야 합니다.**

```
app/(main)/closet/
├── page.tsx
├── page.module.css
└── _components/
    ├── ClosetCard/
    ├── ClosetDetailModal/
    └── Toggle/
```

- 같은 컴포넌트(예: Button)가 여러 페이지에서 사용되더라도, **각 페이지의 `_components/`에 개별 복사본**을 둡니다.
- `frontend/components/`에는 **기본형(Base) 레퍼런스만 유지**합니다. 실제 페이지에서 직접 import하지 않습니다.
- 새 페이지에서 컴포넌트가 필요하면, `frontend/components/`의 기본형을 해당 페이지의 `_components/`로 복사 후 커스텀합니다.

#### `frontend/components/` (전역 기본형 레퍼런스)
| 컴포넌트 | 역할 |
|---|---|
| Header | 루트 레이아웃 전용 (app/layout.tsx) |
| Footer | 루트 레이아웃 전용 (app/layout.tsx) |
| BottomNav | 루트 레이아웃 전용 (app/layout.tsx) |
| OfflineBanner | 전역 네트워크 상태 표시 배너 (app/layout.tsx) |
| Toast | 전역 알림 컴포넌트 (CustomEvent 기반, app/layout.tsx) |
| GlobalLoading | 전역 로딩 스피너 (TanStack Query 기반, app/layout.tsx) |
| Button | 기본형 버튼 레퍼런스 |
| Toggle | 기본형 토글 레퍼런스 |

## API 함수 규칙
```
lib/api/
├── client.ts         # 공통 Axios 기반 API 클라이언트 및 에러 인터셉터 (401 갱신)
├── authAPI.ts        # 인증 API
├── eventAPI.ts       # 이벤트 API
├── hostAPI.ts        # 호스트 API
├── adminAPI.ts       # 관리자 API
├── communityAPI.ts   # 커뮤니티 API
├── orderAPI.ts       # 주문 API
├── contactAPI.ts     # 문의 API
└── index.ts          # re-export
```
- 도메인별 별도 파일로 분리
- 공통 API wrapper(`client.ts`)에 한 번만 정의. 내부적으로 Axios 인스턴스와 인터셉터를 사용하여 전역 네트워크 에러 및 토큰 갱신(401 Unauthorized) 로직을 처리.

## BFF 라우트 핸들러
```
app/api/[...path]/
├── route.ts              # 라우터 (분기만, ~40줄)
└── handlers/
    ├── authHandlers.ts   # 인증 핸들러
    └── proxyHandler.ts   # 일반 API 프록시
```

## 파일 크기 기준
| 크기 | 상태 |
|------|------|
| ~150줄 | ✅ 적정 |
| 150~300줄 | ⚠️ 분리 검토 |
| 300줄 이상 | 🔴 즉시 분리 |
| 500줄 이상 | 🚨 아키텍처 위반 |

## 복잡한 컴포넌트 패턴
- **Feature Component + Custom Hook** 패턴 적용:
  - `use{Feature}.ts` — API 호출, 상태 관리, 비즈니스 로직
  - `{Feature}.tsx` — UI 렌더링만
- 컴포넌트 300줄 초과 시 서브 컴포넌트 분리

## 전역 상태 및 공통 UI 상태 관리
- **비즈니스 상태 (Zustand)**: 인증/인가 등 핵심 비즈니스 로직 위주로 사용 (`useAuthStore` 등). JWT는 Zustand에 저장하지 않고 iron-session으로 관리.
- **로딩 (Global Loading)**: Zustand를 사용하지 않고 **TanStack Query의 `useIsFetching`, `useIsMutating`** 을 구독하여 전역 로딩 자동화 (300ms 딜레이 깜빡임 방지).
- **토스트 (Toast)**: 상태 관리 라이브러리 종속성 없이 **순수 JS `CustomEvent`** (Pub/Sub)를 활용 (`lib/utils/toast.ts`). Axios 인터셉터 등 React 외부 환경에서도 자유롭게 호출 가능.

## v7 코드 테이블 대응
- 상태 비교: `status.id === 2` (숫자 ID 기반)
- 화면 표시: `status.label` → "발행됨"
- 상수 참조: `CodeConstants` (DRAFT=1, PUBLISHED=2, ONGOING=3, ENDED=4, CANCELLED=5)

## 금지 패턴
- ❌ page.tsx에 모든 로직 직접 작성 (150줄 이상이면 _components/ 분리)
- ❌ 컴포넌트를 폴더 없이 단독 파일로 생성
- ❌ CSS Module을 다른 컴포넌트와 공유
- ❌ API 함수를 하나의 파일에 전부 작성
- ❌ BFF route.ts에 모든 핸들러 로직 작성
- ❌ 코드 복붙 (2번 이상 사용 시 공유 컴포넌트로 추출)
