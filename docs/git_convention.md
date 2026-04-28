# 👗 Simul 프로젝트 Git & Jira Convention

---

## 📌 목차

1. [GitHub Flow](#1-github-flow)
2. [도메인 태그](#2-도메인-태그)
3. [Git Convention](#3-git-convention)
4. [Code Convention](#4-code-convention)
5. [PR (Pull Request) Convention](#5-pr-pull-request-convention)
6. [Merge 전략](#6-merge-전략)
7. [자주 쓰는 Git 명령어](#7-자주-쓰는-git-명령어)
8. [Jira 컨벤션](#8-jira-컨벤션)
9. [응답 템플릿](#9-응답-템플릿)

---

## 1. GitHub Flow

### 브랜치 구조

```
main
 └── develop
      └── feat/지라이슈번호 (예: feat/SCRUM-41)
```

### 규칙

- **main** 브랜치에는 프로젝트 최종 안정화 버전 병합 (배포 시 운영 서버 적용)
- **develop** 브랜치는 개발 기능들을 지속적으로 병합하는 통합용 브랜치
- **feature** 브랜치는 각각 **기능 하나씩** 분리하여 로컬 단위로 작업
  - 브랜치명은 Jira **이슈 번호**를 항상 포함하여 작성: `feat/이슈번호` (예: `feat/SCRUM-42`)

---

## 2. 도메인 태그

커밋 메시지, PR 타이틀 등에서 작업 도메인을 명시하기 위해 아래 태그를 사용합니다.

| 태그     | 도메인                  | 설명                              |
| --------| ----------------------- | --------------------------------- |
| `AUTH`  | 인증 / 사용자            | JWT, 소셜로그인, 프로필, 팔로우 등   |
| `TRYON` | AI 가상시착              | AI 연동, 상태(SSE), 크레딧 등      |
| `CLOSET`| 개인 옷장                | 아이템 200개 제한, 카테고리 관리 등  |
| `POST`  | 커뮤니티 피드            | 다중 이미지, 좋아요, 댓글, 신고 등   |
| `TAG`   | 태그 / 검색              | Vision API 자동 태그, 통합 검색 등  |
| `NOTI`  | 알림                    | 4가지 알림 시스템, 읽음 처리 등     |
| `ADMIN` | 관리자                  | 블라인드 처리, 유저 정지 등         |
| `INFRA` | 공통 / 인프라            | BaseEntity, CI/CD, 에러코드 등     |

> 도메인 경계가 애매한 작업은 **주된 변경이 일어나는 도메인** 기준으로 태그를 선택합니다.

---

## 3. Git Convention

### 커밋 타입

| 타입       | 설명                              |
| ---------- | --------------------------------- |
| `fix`      | 버그, 오류 해결                   |
| `feat`     | 새로운 기능 구현                  |
| `refactor` | 코드 개선하는 리팩토링            |
| `env`      | 기타 환경 설정                    |
| `test`     | 테스트 코드 추가                  |
| `chore`    | 그 외 자잘한 작업 및 잡무          |
| `docs`     | README 등 문서 내용 추가 및 변경 |
| `style`    | 레이아웃 등 스타일                |
| `merge`    | 브랜치 병합                       |

### Gitmoji 가이드

| 아이콘 | 코드                           | 설명                        |
| ------ | ------------------------------ | --------------------------- |
| 🎉     | `:tada:`                       | 프로젝트 시작               |
| ✨     | `:sparkles:`                   | 새 기능 (`feat`)            |
| 🎨     | `:art:`                        | 코드의 구조/형태 개선        |
| 🐛     | `:bug:`                        | 버그 수정 (`fix`)           |
| ⚡️    | `:zap:`                        | 성능 개선                   |
| 🚧     | `:construction:`               | 진행 중인 코드 (`chore`)    |
| 💄     | `:lipstick:`                   | UI / style 파일 수정 (`style`)|
| 📝     | `:memo:`                       | 문서 추가/수정 (`docs`)      |
| 🔥     | `:fire:`                       | 코드/파일 삭제              |
| 💚     | `:green_heart:`                | CI 빌드 수정                |
| 👷     | `:construction_worker:`        | CI 빌드 시스템 추가/수정     |
| ♻️     | `:recycle:`                    | 코드 리팩토링 (`refactor`)   |
| 🔧     | `:wrench:`                     | 환경/설정 파일 추가 (`env`)  |
| 💡     | `:bulb:`                       | 주석 추가/수정              |
| ✅     | `:white_check_mark:`           | 테스트 추가/수정 (`test`)    |
| 🔀     | `:twisted_rightwards_arrows:`  | 브랜치 병합 (`merge`)        |

### 커밋 메시지 형식

```
타입: [도메인] 커밋메세지
```

**예시:**

```
feat: [POST] 게시물 작성 API 구현
fix: [TRYON] 크레딧 차감 시점 오류 수정
docs: [INFRA] ERD 알림 테이블 추가
```

> 💡 **Git Hook 자동 변환:** 위와 같이 텍스트 타입으로 커밋하면, 팀원 편의를 위해 `commit-msg` Hook이 자동으로 적절한 이모지를 삽입합니다.
>
> `feat: [AUTH] 소셜 로그인 API 구현` → `✨ feat: [AUTH] 소셜 로그인 API 구현`

---

### ⚠️ Gitmoji 자동 변환 설정 (필수)

**아래 두 가지 방법 중 하나만 선택하세요:**

#### 방법 A. 수동 설정 (1회)

프로젝트 루트에서 아래 명령어를 한번만 실행하면 적용됩니다:

```bash
git config core.hooksPath .githooks
```

#### 방법 B. `npm install` 시 자동 설정

프로젝트 최상단 폴더에 있는 `package.json` 파일의 `scripts` 속성에 아래 내용을 추가해두면, 팀원들이 코드 반영 후 `npm install`을 실행할 때 훅이 자동으로 적용됩니다:

```json
"postinstall": "git config core.hooksPath .githooks"
```

---

## 4. Code Convention

### Java / Spring (Backend)

- **Google Java Formatter** 플러그인 적용
- IntelliJ 환경설정 셋업:
  - `Settings` → `Tools` → `Actions on Save`
  - ✅ **Reformat code**, ✅ **Optimize imports** 체크 확인

### React / Next.js (Frontend)

- **ESLint + Prettier** (Airbnb Style Guide 등) 적용
- 절대 경로 import를 우선으로 사용 (예: `@/components/...`)
- 각 화면 페이지별 컴포넌트는 해당 `page.tsx`와 같은 디렉토리의 `_components/` 안에 위치 (`frontend-rule-for-design.md` 참고)

---

## 5. PR (Pull Request) Convention

기존 MR(Merge Request) 대신 GitHub 환경에 맞춰 PR 작성 규칙을 따릅니다.

### PR 타이틀 작성법

```
[도메인/타입] 기능 설명
```

**예시:**

```
[AUTH/feat] 카카오 소셜 로그인 기능 구현
[TRYON/fix] 시착 완료 이벤트 SSE 버그 수정
[INFRA/refactor] 공통 에러 유틸 리팩토링
```

> **참고:** Jira 이슈가 관련된 경우, PR 본문 하단에 "Closes #SCRUM-41" 등을 작성하거나 Jira-GitHub 연동을 통해 이슈 트래킹이 매끄럽게 연결되도록 합니다.

---

## 6. Merge 전략

| 방향                    | GitHub Merge 옵션 전략 |
| ----------------------- | --------------------- |
| **feature → develop**   | **Squash and Merge**  |
| **develop → main**      | **Rebase and Merge**  |

---

## 7. 자주 쓰는 Git 명령어

| 명령어                                   | 설명                                                |
| ---------------------------------------- | --------------------------------------------------- |
| `git status`                             | 현재 변경사항 보기                                  |
| `git commit -m "commit_message"`         | 커밋 생성                                           |
| `git checkout -b "branch_name"`          | 새로운 브랜치 생성 후 이동                          |
| `git checkout "branch_name"`             | 이미 생성된 브랜치로 이동                           |
| `git log --oneline`                      | 커밋 로그 보기 좋게 확인                            |
| `git reset --soft HEAD~1`                | 직전 커밋 취소 (작업 수정사항은 보존)               |

---

## 8. Jira 컨벤션

GitHub Projects 대신 Jira를 팀 관리 도구로 적극 활용합니다 (Jira Project Name: Simul 등)

### Epic (에픽 대응)

가장 큰 단위의 도메인 묶음 기능을 Epic으로 관리합니다.

- `[도메인] 핵심 기능 그룹 명칭` → 예) `[AUTH] 소셜 로그인 관련`, `[TRYON] AI 가상시착 기능`

### Story / Task (스토리/태스크)

| 항목         | 규칙                                                                 |
| ------------ | -------------------------------------------------------------------- |
| **Status**   | `To Do` → `In Progress` → `Done`                                     |
| **Epic Link**| 생성된 이슈는 반드시 상위 Epic에 연결                                 |
| **Sprint**   | 개발 진행 시 현재 활성화된(진행 중) 스프린트에 포함                    |
| **Estimate** | 에스팀(Original Estimate) 기반으로 시간 필드(예: 3h, 4h 등) 기록      |
| **Assignee** | 담당 개발자 지정                                                    |

### Issue 제목 작성법

```
[도메인] 세부 기능 이름 혹은 태스크 이름
```

**예시:**

- `[AUTH] 유저 JWT 토큰 갱신 기능 구현`
- `[TRYON] AI 이미지 업로드 API 연동`
- `[POST] 게시물 좋아요 카운트 로직 구현`

---

## 9. 응답 템플릿

Simul 프로젝트는 에러 발생 시 공통 규격을 내려보내 클라이언트가 쉽게 처리할 수 있도록 응답 템플릿을 통일합니다.

### 표준 에러 응답

```json
{
  "error_code": "ERR-001",
  "message": "인증 토큰 없음/만료",
  "detail": "토큰이 제공되지 않았거나 만료되었습니다."
}
```

### 도메인별 에러 코드 접두어 가이드

| 접두사   | 에러가 해당하는 도메인 환경     | 예시                        |
| -------- | ------------------------------- | --------------------------- |
| `ERR-0xx`| **공통** 에러                   | `500 서버 불량`, `401 권한` |
| `ERR-1xx`| **AI 가상시착**                 | 잔여 크레딧 부족, 타임아웃  |
| `ERR-2xx`| **옷장**                        | 보관 옷장 개수 상한(200)초과|
| `ERR-3xx`| **커뮤니티 피드 / 태그**        | 최대 5장 이미지 초과 금지   |
| `ERR-4xx`| **관리자 / 신고**               | 중복 신고, 블라인드 경고    |

성공 시 데이터 통신은 각 도메인과 API에 따라 달라지며, HTTP Status Code를 준수합니다.
