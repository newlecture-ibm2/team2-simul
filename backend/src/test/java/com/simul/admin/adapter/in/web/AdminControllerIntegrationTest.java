package com.simul.admin.adapter.in.web;

import com.simul.post.domain.model.Post;
import com.simul.post.domain.model.PostStatus;
import com.simul.post.adapter.out.persistence.PostJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostJpaRepository postJpaRepository;

    @Test
    @DisplayName("관리자가 특정 게시물을 강제 블라인드 처리할 수 있어야 한다")
    void blindPost_success() throws Exception {
        // given
        UUID testUserId = UUID.randomUUID();
        Post post = Post.builder()
                .userId(testUserId)
                .caption("블라인드 테스트 캡션")
                .status(PostStatus.COMPLETED)
                .build();
        Post savedPost = postJpaRepository.save(post);
        
        UUID adminUserId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken adminAuth = new UsernamePasswordAuthenticationToken(
                adminUserId, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        // when
        mockMvc.perform(patch("/admin/posts/" + savedPost.getPostId() + "/blind")
                        .with(authentication(adminAuth))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        // then
        Post updatedPost = postJpaRepository.findById(savedPost.getPostId()).orElseThrow();
        assertThat(updatedPost.getIsBlinded()).isTrue();
    }

    @Test
    @DisplayName("관리자가 특정 게시물의 블라인드를 해제할 수 있어야 한다")
    void unblindPost_success() throws Exception {
        // given
        UUID testUserId = UUID.randomUUID();
        Post post = Post.builder()
                .userId(testUserId)
                .caption("블라인드 해제 테스트 캡션")
                .status(PostStatus.COMPLETED)
                .isBlinded(true)
                .build();
        Post savedPost = postJpaRepository.save(post);

        UUID adminUserId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken adminAuth = new UsernamePasswordAuthenticationToken(
                adminUserId, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        // when
        mockMvc.perform(patch("/admin/posts/" + savedPost.getPostId() + "/unblind")
                        .with(authentication(adminAuth))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        // then
        Post updatedPost = postJpaRepository.findById(savedPost.getPostId()).orElseThrow();
        assertThat(updatedPost.getIsBlinded()).isFalse();
    }
}
