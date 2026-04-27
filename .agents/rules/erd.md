---
trigger: always_on
---

# Simul ERD 핵심 규칙

> 원본: `docs/simul-erd.md`

## 테이블 목록 및 핵심 컬럼

### users
`user_id(PK,UUID)`, provider, provider_id, UNIQUE(provider,provider_id), nickname, bio, profile_image_url, is_public(DEFAULT true), role(USER/ADMIN), is_active(DEFAULT true), created_at, deleted_at

### base_images
`base_image_id(PK)`, user_id(FK→users), image_url, source_post_id(FK→posts, nullable), created_at, deleted_at
- 시착 결과를 다시 베이스로 사용 가능 (순환 구조)

### clothing_images
`image_id(PK)`, image_url, uploader_id(FK→users), created_at

### closet_items
`item_id(PK)`, user_id(FK), image_id(FK→clothing_images), category(ENUM: top/bottom/outer/shoes/accessory), memo(max100), try_count(DEFAULT 0), created_at, deleted_at

### posts
`post_id(PK)` = job_id, user_id(FK), base_image_id(FK,nullable), item_id(FK,nullable), image_url(nullable,대표), status(processing/completed/failed), caption, is_public(DEFAULT false), is_blinded(DEFAULT false), report_count(DEFAULT 0), like_count, view_count, created_at, deleted_at

### post_images
`post_image_id(PK)`, post_id(FK), image_url, sort_order(0부터), created_at

### tags
`tag_id(PK)`, name(UNIQUE,max20), category(nullable), usage_count(DEFAULT 0), created_at

### post_tags
post_tag_id(PK), post_id(FK), tag_id(FK), UNIQUE(post_id,tag_id), created_at

### tryon_credits
`credit_id(PK)`, user_id(FK), used_at, job_id(FK→posts)

### comments
`comment_id(PK)`, post_id(FK), user_id(FK), parent_comment_id(FK,nullable), depth(1~2), content(max200), created_at, deleted_at

### follows
`follow_id(PK)`, follower_id(FK), following_id(FK), UNIQUE(follower,following), created_at

### likes
`like_id(PK)`, post_id(FK), user_id(FK), UNIQUE(post_id,user_id), created_at

### reports
`report_id(PK)`, post_id(FK), reporter_id(FK), reason, UNIQUE(post_id,reporter_id), created_at

### notifications
`notification_id(PK)`, recipient_id(FK), actor_id(FK,nullable), type(TRYON_COMPLETE/LIKE/COMMENT/FOLLOW_POST), reference_id(nullable), message(max200), is_read(DEFAULT false), created_at

## 핵심 관계
- users 1:N base_images, closet_items, posts, comments, likes, follows, reports, notifications
- posts 1:N post_images, post_tags, comments, likes, reports
- tags 1:N post_tags
- clothing_images 1:N closet_items
- posts ↔ base_images (순환: 시착 결과→다음 베이스)
