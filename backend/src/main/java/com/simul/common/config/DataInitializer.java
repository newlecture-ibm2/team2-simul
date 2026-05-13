package com.simul.common.config;

import com.simul.closet.adapter.out.persistence.ClothingImageJpaRepository;
import com.simul.closet.adapter.out.persistence.ClosetCollectionJpaRepository;
import com.simul.closet.adapter.out.persistence.ClosetItemJpaRepository;
import com.simul.closet.adapter.out.persistence.CollectionItemJpaRepository;
import com.simul.closet.domain.model.Category;
import com.simul.closet.domain.model.ClosetCollection;
import com.simul.closet.domain.model.ClosetItem;
import com.simul.closet.domain.model.ClothingImage;
import com.simul.closet.domain.model.CollectionItem;
import com.simul.post.adapter.out.persistence.PostJpaRepository;
import com.simul.post.adapter.out.persistence.PostLikeJpaRepository;
import com.simul.post.domain.model.Post;
import com.simul.post.domain.model.PostImage;
import com.simul.post.domain.model.PostLike;
import com.simul.post.domain.model.PostStatus;
import com.simul.user.adapter.out.persistence.UserJpaEntity;
import com.simul.user.adapter.out.persistence.UserJpaRepository;
import com.simul.user.domain.model.Gender;
import com.simul.user.domain.model.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 로컬 개발용 초기 데이터 생성기
 * - local 프로필에서만 동작
 * - 유저 3명 (ADMIN 1, USER 2)
 * - 각 유저당 옷장 아이템 10개
 * - 게시물, 팔로우 관계 등 개발에 도움 되는 데이터 포함
 */
@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserJpaRepository userJpaRepository;
    private final ClothingImageJpaRepository clothingImageJpaRepository;
    private final ClosetItemJpaRepository closetItemJpaRepository;
    private final ClosetCollectionJpaRepository closetCollectionJpaRepository;
    private final CollectionItemJpaRepository collectionItemJpaRepository;
    private final PostJpaRepository postJpaRepository;
    private final PostLikeJpaRepository postLikeJpaRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    // 고정 UUID (개발 시 참조 편의용)
    private static final UUID ADMIN_ID  = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID USER1_ID  = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID USER2_ID  = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Override
    @Transactional
    public void run(String... args) {
        if (userJpaRepository.count() > 0) {
            log.info("✅ 기존 데이터가 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        log.info("🌱 로컬 개발용 초기 데이터 생성을 시작합니다...");

        // ==========================================
        // 1. 유저 3명 생성
        // ==========================================
        UserJpaEntity admin = createUser(ADMIN_ID, "kakao", "kakao_admin_001",
                "관리자", "SimulAdmin", null, Gender.UNKNOWN, "SIMUL 관리자 계정입니다.", Role.ADMIN);
        
        // USER1을 이메일 로그인 사용자로 변경
        UserJpaEntity user1 = createUser(USER1_ID, "email", "user1@simul.com",
                "정찬우", "chim-chan-man", "simul1234", Gender.MALE, "패션을 사랑하는 정찬우입니다 🌟", Role.USER);
        
        // USER2를 이메일 로그인 사용자로 변경 (추가)
        UserJpaEntity user2 = createUser(USER2_ID, "email", "user2@simul.com",
                "이우석", "rainstone_lee", "simul1234", Gender.MALE, "일상 속 스타일을 찾아서 👕", Role.USER);

        userJpaRepository.saveAll(List.of(admin, user1, user2));
        log.info("  → 유저 3명 생성 완료 (ADMIN: {}, USER1: {}, USER2: {})", ADMIN_ID, USER1_ID, USER2_ID);

        // ==========================================
        // 2. 각 유저별 컬렉션 생성
        // ==========================================
        ClosetCollection adminCol1 = createCollection(ADMIN_ID, "관리자 컬렉션", "/uploads/images/sample/post_07.jpg", 0);
        ClosetCollection user1Col1 = createCollection(USER1_ID, "출근룩", "/uploads/images/sample/post_01.jpg", 0);
        ClosetCollection user1Col2 = createCollection(USER1_ID, "데이트룩", "/uploads/images/sample/post_02.jpg", 1);
        ClosetCollection user2Col1 = createCollection(USER2_ID, "캐주얼", "/uploads/images/sample/post_04.jpg", 0);
        ClosetCollection user2Col2 = createCollection(USER2_ID, "포멀", "/uploads/images/sample/post_05.jpg", 1);

        closetCollectionJpaRepository.saveAll(List.of(adminCol1, user1Col1, user1Col2, user2Col1, user2Col2));
        log.info("  → 컬렉션 5개 생성 완료");

        // ==========================================
        // 3. 각 유저별 옷장 아이템 10개 생성
        // ==========================================
        List<ClothingImage> allImages = new ArrayList<>();
        List<ClosetItem> allItems = new ArrayList<>();
        List<CollectionItem> allCollectionItems = new ArrayList<>();

        String[][] itemData = {
                {"TOP",       "화이트 오버핏 티셔츠"},
                {"TOP",       "네이비 스트라이프 셔츠"},
                {"TOP",       "블랙 크루넥 니트"},
                {"BOTTOM",    "워싱 데님 팬츠"},
                {"BOTTOM",    "블랙 슬랙스"},
                {"OUTER",     "베이지 트렌치코트"},
                {"OUTER",     "블랙 레더 자켓"},
                {"SHOES",     "화이트 캔버스 스니커즈"},
                {"ACCESSORY", "실버 체인 목걸이"},
                {"ACCESSORY", "블랙 버킷햇"}
        };

        createItemsForUser(ADMIN_ID, adminCol1, itemData, "admin", allImages, allItems, allCollectionItems);
        createItemsForUser(USER1_ID, user1Col1, itemData, "user1", allImages, allItems, allCollectionItems);
        createItemsForUser(USER2_ID, user2Col1, itemData, "user2", allImages, allItems, allCollectionItems);

        clothingImageJpaRepository.saveAll(allImages);
        closetItemJpaRepository.saveAll(allItems);
        collectionItemJpaRepository.saveAll(allCollectionItems);
        log.info("  → 옷장 아이템 총 {}개 생성 완료 (유저당 10개)", allItems.size());

        // ==========================================
        // 4. 게시물 생성 (공개 게시물)
        // ==========================================
        List<Post> posts = new ArrayList<>();

        String[][] postData = {
                {"1", "오늘의 출근룩 #OOTD 🌸", "/uploads/images/sample/post_01.jpg"},
                {"1", "주말 카페 나들이 ☕️", "/uploads/images/sample/post_02.jpg"},
                {"1", "봄맞이 새 자켓 득템!", "/uploads/images/sample/post_03.jpg"},
                {"2", "데일리 캐주얼룩 👖", "/uploads/images/sample/post_04.jpg"},
                {"2", "비 오는 날 코디 ☔️", "/uploads/images/sample/post_05.jpg"},
                {"2", "운동화 신고 한 컷 👟", "/uploads/images/sample/post_06.jpg"},
                {"0", "관리자 테스트 게시물", "/uploads/images/sample/post_07.jpg"},
        };

        UUID[] userIds = {ADMIN_ID, USER1_ID, USER2_ID};

        for (int i = 0; i < postData.length; i++) {
            int userIdx = Integer.parseInt(postData[i][0]);
            String caption = postData[i][1];
            String imageUrl = postData[i][2];

            Post post = Post.builder()
                    .userId(userIds[userIdx])
                    .imageUrl(imageUrl)
                    .status(PostStatus.COMPLETED)
                    .caption(caption)
                    .isPublic(true)
                    .likeCount(0)
                    .viewCount((int) (Math.random() * 200))
                    .build();

            PostImage img1 = PostImage.builder()
                    .post(post)
                    .imageUrl(imageUrl)
                    .sortOrder(0)
                    .build();
            PostImage img2 = PostImage.builder()
                    .post(post)
                    .imageUrl("/uploads/images/sample/post_" + String.format("%02d", i + 1) + "_sub.jpg")
                    .sortOrder(1)
                    .build();

            post.addImage(img1);
            post.addImage(img2);
            posts.add(post);
        }

        postJpaRepository.saveAll(posts);
        log.info("  → 게시물 {}개 생성 완료 (각 2장 이미지 포함)", posts.size());

        // ==========================================
        // 5. 좋아요(PostLike) 생성
        // ==========================================
        List<PostLike> likes = new ArrayList<>();
        
        for (int i = 0; i < posts.size(); i++) {
            Post post = posts.get(i);
            
            likes.add(PostLike.builder()
                    .postId(post.getPostId())
                    .userId(USER1_ID)
                    .build());
            post.incrementLikeCount();
            
            if (i % 2 != 0) {
                likes.add(PostLike.builder()
                        .postId(post.getPostId())
                        .userId(USER2_ID)
                        .build());
                post.incrementLikeCount();
            }
        }
        
        postLikeJpaRepository.saveAll(likes);
        postJpaRepository.saveAll(posts);
        log.info("  → 게시물 좋아요(Like) {}개 생성 완료", likes.size());

        // ==========================================
        // 완료 로그
        // ==========================================
        log.info("🌱 초기 데이터 생성 완료!");
        log.info("  📌 고정 UUID 참조:");
        log.info("     ADMIN  = {}", ADMIN_ID);
        log.info("     USER1  = {}", USER1_ID);
        log.info("     USER2  = {}", USER2_ID);
    }

    // ==========================================
    // Helper Methods
    // ==========================================

    private UserJpaEntity createUser(UUID userId, String provider, String providerId,
                                      String name, String nickname, String password, Gender gender, String bio, Role role) {
        String encodedPassword = (password != null) ? passwordEncoder.encode(password) : null;
        return UserJpaEntity.builder()
                .userId(userId)
                .provider(provider)
                .providerId(providerId)
                .name(name)
                .nickname(nickname)
                .password(encodedPassword)
                .gender(gender)
                .bio(bio)
                .profileImageUrl("/uploads/images/sample/profile_" + nickname + ".jpg")
                .isPublic(true)
                .role(role)
                .isActive(true)
                .build();
    }

    private ClosetCollection createCollection(UUID userId, String name, String coverImageUrl, int sortOrder) {
        return ClosetCollection.builder()
                .userId(userId)
                .name(name)
                .coverImageUrl(coverImageUrl)
                .sortOrder(sortOrder)
                .build();
    }

    private void createItemsForUser(UUID userId, ClosetCollection collection,
                                     String[][] itemData, String prefix,
                                     List<ClothingImage> imageAccumulator,
                                     List<ClosetItem> itemAccumulator,
                                     List<CollectionItem> collectionItemAccumulator) {
        for (int i = 0; i < itemData.length; i++) {
            Category category = Category.valueOf(itemData[i][0]);
            String memo = itemData[i][1];
            String imageUrl = "/uploads/images/sample/" + prefix + "_item_" + String.format("%02d", i + 1) + ".jpg";

            ClothingImage clothingImage = new ClothingImage(imageUrl, userId);
            imageAccumulator.add(clothingImage);

            ClosetItem closetItem = ClosetItem.builder()
                    .userId(userId)
                    .clothingImage(clothingImage)
                    .category(category)
                    .memo(memo)
                    .sortOrder(i)
                    .build();
            itemAccumulator.add(closetItem);

            // CollectionItem 매핑 생성
            CollectionItem collectionItem = CollectionItem.builder()
                    .collection(collection)
                    .item(closetItem)
                    .sortOrder(i)
                    .build();
            collectionItemAccumulator.add(collectionItem);
        }
    }
}
