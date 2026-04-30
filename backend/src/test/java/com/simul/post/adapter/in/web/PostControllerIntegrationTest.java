package com.simul.post.adapter.in.web;

import com.simul.post.domain.model.Post;
import com.simul.post.adapter.out.persistence.PostJpaRepository;
import com.simul.tag.adapter.out.persistence.PostTagJpaRepository;
import com.simul.tag.adapter.out.persistence.TagJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // 테스트 종료 후 데이터 롤백
public class PostControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostJpaRepository postJpaRepository;

    @Autowired
    private TagJpaRepository tagJpaRepository;

    @Autowired
    private PostTagJpaRepository postTagJpaRepository;

    @Test
    @DisplayName("게시물 작성 성공 - 이미지와 태그가 정상적으로 저장되어야 한다")
    void createPost_success() throws Exception {
        // given
        UUID testUserId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                testUserId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        MockMultipartFile image1 = new MockMultipartFile(
                "images", "test1.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy image content 1".getBytes());
        MockMultipartFile image2 = new MockMultipartFile(
                "images", "test2.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy image content 2".getBytes());

        // when
        mockMvc.perform(multipart(HttpMethod.POST, "/api/posts")
                        .file(image1)
                        .file(image2)
                        .param("caption", "테스트 캡션입니다.")
                        .param("isPublic", "true")
                        .param("tags", "OOTD", "Denim", "Spring")
                        .with(authentication(auth))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.postId").exists());

        // then
        List<Post> posts = postJpaRepository.findAll();
        assertThat(posts).hasSize(1);
        Post savedPost = posts.get(0);
        
        assertThat(savedPost.getUserId()).isEqualTo(testUserId);
        assertThat(savedPost.getCaption()).isEqualTo("테스트 캡션입니다.");
        assertThat(savedPost.getIsPublic()).isTrue();
        assertThat(savedPost.getImages()).hasSize(2); // 이미지가 2개 저장되었는지 확인

        // Tag 연관관계 검증
        assertThat(tagJpaRepository.findAll()).hasSize(3); // 새로운 태그 3개가 생성되었는지 확인
        assertThat(postTagJpaRepository.findAll()).hasSize(3); // 게시물과 태그 간의 매핑이 3개 생겼는지 확인
    }

    @Test
    @DisplayName("로그인하지 않은 사용자가 게시물을 작성하면 4xx 에러를 반환해야 한다")
    void createPost_unauthorized() throws Exception {
        // given
        MockMultipartFile image1 = new MockMultipartFile(
                "images", "test1.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy".getBytes());

        // when & then
        mockMvc.perform(multipart(HttpMethod.POST, "/api/posts")
                        .file(image1)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }
}
