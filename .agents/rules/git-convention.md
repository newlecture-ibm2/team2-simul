---
trigger: always_on
---

# Simul Git 컨벤션

> 원본: `docs/git_convention.md`

## 브랜치 전략 (GitHub Flow)
```
main ← develop ← feat/이슈번호
```
- **main**: 배포 전용, 직접 커밋 금지
- **develop**: 개발 통합 브랜치
- **feat/이슈번호**: 기능 하나씩 개발 (예: `feat/42`)

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
| `INFRA` | 공통 인프라 |

## 커밋 메시지
```
타입: [도메인] 설명
```
**타입**: `feat` | `fix` | `refactor` | `test` | `docs` | `style` | `chore` | `env`

**예시**:
```
feat: [POST] 게시물 작성 API 구현
fix: [TRYON] 크레딧 차감 시점 오류 수정
docs: [INFRA] ERD 알림 테이블 추가
```

## Merge 전략
| 방향 | 전략 |
|------|------|
| feature → develop | **Squash and Merge** |
| develop → main | **Rebase and Merge** |

## MR 타이틀
```
[도메인/타입] 기능 설명
```
예: `[POST/feat] 다중 이미지 게시물 작성`, `[AUTH/fix] JWT 갱신 오류 수정`

## Code Convention
- **Backend**: Google Java Formatter, IntelliJ Reformat + Optimize imports on save
- **Frontend**: ESLint + Prettier (Airbnb Style Guide)
