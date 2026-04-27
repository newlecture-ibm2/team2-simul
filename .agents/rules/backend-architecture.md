---
trigger: always_on
---

# Simul 백엔드 아키텍처 규칙

> 원본: `docs/simul-backend-architecture.md`

## 헥사고날 아키텍처

```
[Web Adapter] → [Input Port] ← [Service] → [Output Port] ← [Persistence Adapter]
  (Outside)     (Interface)     (Inside)     (Interface)       (Outside)
```

- 의존성은 **바깥→안쪽** 방향만 허용
- Domain/Application은 Adapter에 의존하지 않음
- 외부 서비스(AI, Vision API)는 Output Port로 추상화

## 9개 도메인 책임

| 도메인 | 소유 테이블 | 핵심 규칙 |
|--------|-----------|----------|
| Auth | — | OAuth2 검증, JWT 발급/갱신, User Port로 회원 접근 |
| User | users, follows | 프로필CRUD, 팔로우, UNIQUE(follower,following) |
| Closet | closet_items, clothing_images | 200개 상한, Deep Copy, try_count |
| TryOn | base_images, tryon_credits | 크레딧 5회/일, AI 비동기, SSE, 베이스 순환 |
| Post | posts, post_images, comments, likes, reports | 다중이미지 5장, 좋아요토글, 2-depth댓글, 신고5회→블라인드 |
| Tag | tags, post_tags | Vision API 태그추출, 자동완성, Post가 Tag 호출(단방향) |
| Notification | notifications | 4유형 알림, 본인 활동 알림 미생성, 읽음 처리 |
| Admin | — | 타 도메인 Port 호출 (블라인드, 정지, 크레딧 지급) |
| common | — | BaseEntity, 에러코드, 보안, 이미지업로드, 페이지네이션 |

## 패키지 구조 (도메인별)
```
{domain}/
  ├── domain/model/          # Entity, VO
  ├── application/
  │   ├── port/in/           # UseCase 인터페이스
  │   ├── port/out/          # Persistence/External Port
  │   ├── service/           # UseCase 구현체
  │   └── dto/               # Command, Response
  └── adapter/
      ├── in/web/            # Controller + DTO
      └── out/
          ├── persistence/   # JPA Adapter
          └── {external}/    # AI, Vision 등 외부 API
```

## 의존 방향 (순환 절대 금지)
```
Auth → User | Closet → User | Post → User, Tag
TryOn → Closet, Post | Admin → User, Post, TryOn
Tag → (없음) | Notification ← TryOn, Post, User
```

## 교차 도메인 규칙
1. 타 도메인은 **Input Port(UseCase) 인터페이스**로만 접근
2. **Repository/Entity/Adapter 직접 접근 금지**
3. 통합 검색은 **SearchController가 오케스트레이션** (Post+Tag 양쪽 호출, 자체 로직 없음)

## 핵심 흐름

### AI 시착 생성
1. 크레딧 확인 → 2. 베이스/옷 존재 확인 → 3. Post 도메인으로 비공개 게시물 생성
4. AI 서비스 비동기 호출 → 5. 완료 시 크레딧 차감 + 결과 업데이트 + 옷장 try_count 증가

### 게시물 작성 (시착 직후)
SCR-024 → SCR-012: 시착 결과 이미지 첫 번째 자동 완성, 추가 이미지 선택 가능, Vision API 태그 분석→Tag 도메인의 AttachTagsToPostUseCase 호출

### 이미지 저장
- MVP: 서버 로컬 (`/uploads/images/`), FileStorageService 추상화
- 향후: S3 Adapter 교체만으로 전환 (헥사고날 장점)
