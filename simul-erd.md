# Simul ERD (Entity-Relationship Diagram)

> **기준 문서**: `simul-functional-spec.md`, `simul-api-spec.md`  
> **기능 변경 반영 목록**: 가상 시착용 베이스 사람 이미지(`base_images`) 영구 보관 및 AI 시착 결과를 베이스 이미지로 재사용하는 순환 로직 추가.

## 시각화 다이어그램 (Mermaid)
이 다이어그램은 Github Markdown이나 최신 Markdown 뷰어에서 자동으로 다이어그램 맵으로 변환되어 출력됩니다.

```mermaid
erDiagram
    users {
        UUID user_id PK
        ENUM provider "kakao, naver, google, email"
        VARCHAR provider_id 
        VARCHAR nickname
        VARCHAR bio
        TEXT profile_image_url
        BOOLEAN is_public "DEFAULT true"
        ENUM role "user, admin"
        BOOLEAN is_active "DEFAULT true (정지 여부)"
        TIMESTAMP created_at
        TIMESTAMP deleted_at "nullable (소프트 딜리트)"
    }

    base_images {
        UUID base_image_id PK
        UUID user_id FK
        TEXT image_url
        UUID source_post_id FK "nullable (기존 시착 결과를 재사용한 경우 출처)"
        TIMESTAMP created_at
        TIMESTAMP deleted_at
    }

    clothing_images {
        UUID image_id PK
        TEXT image_url
        UUID uploader_id FK "최초 업로더"
        TIMESTAMP created_at
    }

    closet_items {
        UUID item_id PK
        UUID user_id FK
        UUID image_id FK
        ENUM category "top, bottom, outer, shoes, accessory"
        VARCHAR memo
        INT try_count
        TIMESTAMP created_at
        TIMESTAMP deleted_at
    }

    posts {
        UUID post_id PK "시착 잡의 job_id 공유"
        UUID user_id FK
        UUID base_image_id FK "시착에 사용한 내 몸 베이스 사진"
        UUID item_id FK "nullable (시착 원본 옷 출처)"
        TEXT image_url "nullable (처리 전 null)"
        ENUM status "processing, completed, failed"
        VARCHAR caption "nullable"
        BOOLEAN is_public "DEFAULT false (비공개 우선)"
        BOOLEAN is_blinded "DEFAULT false (신고 누적 자동 블라인드)"
        INT report_count "DEFAULT 0"
        INT like_count
        INT view_count
        TIMESTAMP created_at
        TIMESTAMP deleted_at
    }

    tryon_credits {
        UUID credit_id PK
        UUID user_id FK
        TIMESTAMP used_at
        UUID job_id FK "references posts.post_id"
    }

    comments {
        UUID comment_id PK
        UUID post_id FK
        UUID user_id FK
        UUID parent_comment_id FK "nullable (대댓글)"
        INT depth "1 or 2"
        VARCHAR content "max: 200"
        TIMESTAMP created_at
        TIMESTAMP deleted_at
    }

    follows {
        UUID follow_id PK
        UUID follower_id FK "references users"
        UUID following_id FK "references users"
        TIMESTAMP created_at
    }

    likes {
        UUID like_id PK
        UUID post_id FK
        UUID user_id FK
        TIMESTAMP created_at
        UNIQUE post_id_user_id "(post_id, user_id)"
    }

    reports {
        UUID report_id PK
        UUID post_id FK
        UUID reporter_id FK "references users"
        VARCHAR reason
        TIMESTAMP created_at
        UNIQUE post_id_reporter_id "(post_id, reporter_id)"
    }

    %% Relationships
    users ||--o{ base_images : "registers (모델 샷 등록)"
    users ||--o{ clothing_images : "uploads (최초 등록)"
    users ||--o{ closet_items : "owns (옷장 보관)"
    clothing_images ||--o{ closet_items : "mapped_to (옷장 복사본 연결)"
    
    users ||--o{ posts : "creates_or_tryon (시착 등록 및 피드)"
    base_images ||--o{ posts : "used_as_model (시착 모델로 사용)"
    posts |o--o{ base_images : "converted_to_model (AI 결과를 다시 모델로 사용)"
    closet_items |o--o{ posts : "used_in_tryon (입어본 옷 출처 연결)"
    
    users ||--o{ tryon_credits : "spends (크레딧 사용 내역)"
    posts ||--o| tryon_credits : "consumes (시착 작업 job_id 결합)"
    
    users ||--o{ comments : "writes"
    posts ||--o{ comments : "has"
    comments |o--o{ comments : "replies_to (대댓글 구조)"

    users ||--o{ likes : "likes (좋아요 누른 유저)"
    posts ||--o{ likes : "has_likes (좋아요 받은 게시물)"
    
    users ||--o{ follows : "follower (팔로우 거는 쪽)"
    users ||--o{ follows : "following (팔로우 받는 쪽)"
    
    users ||--o{ reports : "reports (신고 제출)"
    posts ||--o{ reports : "is_reported (신고당한 게시물)"
```

## 신규 도입된 설계 포인트 (베이스 이미지 순환 구조)
1. **`base_images` 테이블 분리 확장**: 사용자가 AI 가상시착에 사용하는 "내 몸 모델(Base Image)" 사진들을 DB에 온전히 기록하여 관리합니다.
2. **시착 결과를 다시 내 모델로 사용(`source_post_id`)**: 단순히 갤러리에서 새로 올리는 것뿐만 아니라, 과거에 너무 만족스럽게 합성된 '시착 완료 사진(`posts`)' 자체를 다음 시착의 내 몸 모델 사진으로 그대로 승계등록할 수 있도록 FK 자기 참조 흐름 구조를 확립했습니다.
