---
trigger: always_on
---

# Simul Git & Jira 컨벤션

> 원본: `docs/git_convention.md`

## 브랜치 전략 (GitHub Flow)
```
main ← develop ← feat/지라이슈번호
```
- **main**: 배포 전용, 직접 커밋 금지
- **develop**: 개발 통합 브랜치
- **feat/지라이슈번호**: 기능 하나씩 개발 (예: `feat/SCRUM-42`)

## 도메인 태그
| 태그 | 도메인 |
|------|--------|
| `AUTH` | 인증/사용자 |
| `TRYON` | AI 가상시착 |
| `CLOSET` | 개인 옷장 |
| `POST` | 커뮤니티 피드 (게시물) |
| `TAG` | 태그/검색 |
| `NOTI` | 알림 |
| `ADMIN` | 관리자 |
| `INFRA` | 공통/인프라 |

## 커밋 메시지
```
타입: [도메인] 설명
```
**타입**: `feat` (새 기능) | `fix` (버그 수정) | `refactor` (리팩토링) | `test` (테스트) | `docs` (문서) | `style` (UI/스타일) | `chore` (잡무/진행중) | `env` (설정/환경)

**예시**:
```
feat: [POST] 게시물 작성 API 구현
fix: [TRYON] 크레딧 차감 시점 오류 수정
docs: [INFRA] ERD 알림 테이블 추가
```

> **참고**: `commit-msg` 훅으로 인해 위처럼 작성 시 자동으로 Gitmoji(✨, 🐛 등)가 맨 앞에 삽입됩니다.

## PR (Pull Request) 타이틀
```
[도메인/타입] 기능 설명
```
예: `[POST/feat] 다중 이미지 게시물 작성`, `[AUTH/fix] JWT 갱신 오류 수정`
(PR 내용 란에 `Closes #SCRUM-42` 등 관련된 지라 이슈 번호를 기재할 것)

## Merge 전략
| 방향 | 전략 |
|------|------|
| feature → develop | **Squash and Merge** |
| develop → main | **Rebase and Merge** |

## Jira Issue 컨벤션
- **Epic**: 도메인 단위 큰 기능 묶음 (예: `[TRYON] AI 가상시착 기능`)
- **Story / Task**: `[도메인] 세부 기능 이름` (예: `[TRYON] AI 이미지 업로드 API 연동`)
  - **Status**: `To Do` → `In Progress` → `Done`
  - **Estimate**: 시간(예: `3h`) 기록 기반
  - 관련된 **Epic**과 활성 **Sprint**에 필수 할당

## Code Convention
- **Backend**: Google Java Formatter, IntelliJ Reformat + Optimize imports on save
- **Frontend**: ESLint + Prettier 적용, 컴포넌트는 해당 `page.tsx`와 같은 디렉토리 `_components/` 폴더 내에 배치
