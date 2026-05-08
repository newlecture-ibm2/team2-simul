---
trigger: always_on
---

# Simul 기술 스택 및 MVP 범위

> 원본: `docs/simul-mvp-spec.md`

## 서비스 개요
AI 가상시착 → 개인 옷장 → 커뮤니티 피드 패션 소셜 플랫폼 (반응형 웹, 모바일 최적화)

## 기술 스택

### 백엔드
| 기술 | 버전 |
|------|------|
| Java | 21 (LTS) |
| Spring Boot | 3.3 |
| Spring Data JPA + Hibernate | — |
| PostgreSQL | 17+ |
| Spring Security + OAuth2 | — |
| jjwt | 0.12+ |
| SpringDoc OpenAPI | 2.x |
| Spring MVC SseEmitter | — |
| Image Storage | 서버 로컬 파일 시스템 (`/uploads/images/`) |
| Gradle (Kotlin DSL) | 8+ |
| JUnit 5 + Mockito + AssertJ | — |

### 프론트엔드
| 기술 | 버전 |
|------|------|
| TypeScript | 5.5 |
| Next.js (App Router) | 15 |
| React | 19 |
| Tailwind CSS | 3.4 |
| Zustand | 4.5 |
| TanStack Query | 5 |
| Axios | 1.7 |
| React Hook Form + Zod | 7 + 3 |

### 인프라
- 자체 서버 (또는 AWS EC2) | GitHub Actions CI/CD | Docker

### 외부 API
- AI 가상시착 API (REST + SSE)
- Google Vision API (Label/Web Detection)
- 카카오/네이버/구글 OAuth2

## MVP 핵심 3기능
1. **AI 가상시착** (P1) — 사진+옷→합성, 크레딧 5회/일, SSE 상태, 비공개 게시물 자동 생성
2. **개인 옷장** (P2) — 아이템CRUD 200개 상한, 4경로 추가, Deep Copy, Vision검색[Should]
3. **커뮤니티 피드** (P3) — 다중이미지 5장, 태그 자동추출 10개, 좋아요[Should], 댓글[Could], 신고[Should]
4. **알림** — 시착완료/좋아요/댓글/팔로우게시 4유형, In-App 알림

## 아키텍처
- **헥사고날 아키텍처** (Ports & Adapters)
- Post→Tag 단방향, 순환 금지
- 9개 도메인: Auth, User, Closet, TryOn, Post, Tag, Notification, Admin, common
