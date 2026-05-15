package com.simul.tryon.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simul.closet.adapter.out.persistence.ClosetItemJpaRepository;
import com.simul.closet.adapter.out.persistence.ClothingImageJpaRepository;
import com.simul.closet.domain.model.Category;
import com.simul.closet.domain.model.ClosetItem;
import com.simul.closet.domain.model.ClothingImage;
import com.simul.post.adapter.out.persistence.PostJpaRepository;
import com.simul.post.domain.model.Post;
import com.simul.post.domain.model.PostStatus;
import com.simul.tryon.adapter.out.persistence.BaseImageJpaRepository;
import com.simul.tryon.adapter.out.persistence.TryonCreditJpaRepository;
import com.simul.tryon.domain.model.BaseImage;
import com.simul.tryon.domain.model.TryonCredit;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TryonFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BaseImageJpaRepository baseImageJpaRepository;

    @Autowired
    private ClothingImageJpaRepository clothingImageJpaRepository;

    @Autowired
    private ClosetItemJpaRepository closetItemJpaRepository;

    @Autowired
    private PostJpaRepository postJpaRepository;

    @Autowired
    private TryonCreditJpaRepository tryonCreditJpaRepository;

    @Test
    @DisplayName("시착 생성 요청 후 job 조회에서 베이스 이미지와 완료 결과를 확인할 수 있다")
    void tryonGenerateAndJobLookupFlow() throws Exception {
        UUID userId = UUID.randomUUID();
        var auth = new UsernamePasswordAuthenticationToken(
                userId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        BaseImage baseImage = baseImageJpaRepository.save(new BaseImage(userId, "/uploads/images/tryon/base.png"));
        ClothingImage clothingImage =
                clothingImageJpaRepository.save(new ClothingImage("/uploads/images/closet/top.png", userId));
        ClosetItem closetItem = closetItemJpaRepository.save(ClosetItem.builder()
                .userId(userId)
                .clothingImage(clothingImage)
                .category(Category.TOP)
                .memo("test top")
                .sortOrder(1)
                .build());

        String body = """
                {
                  "base_image_id": "%s",
                  "item_id": "%s"
                }
                """.formatted(baseImage.getId(), closetItem.getId());

        String responseBody = mockMvc.perform(post("/tryon/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(authentication(auth)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.job_id").exists())
                .andExpect(jsonPath("$.status").value("processing"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID jobId = UUID.fromString(objectMapper.readTree(responseBody).get("job_id").asText());
        Post post = postJpaRepository.findById(jobId).orElseThrow();

        assertThat(post.getStatus()).isEqualTo(PostStatus.PROCESSING);
        assertThat(post.getBaseImageId()).isEqualTo(baseImage.getId());
        assertThat(post.getItemId()).isEqualTo(closetItem.getId());
        assertThat(tryonCreditJpaRepository.existsByJobId(jobId)).isFalse();

        mockMvc.perform(get("/tryon/jobs/{jobId}", jobId).with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.job_id").value(jobId.toString()))
                .andExpect(jsonPath("$.status").value("processing"))
                .andExpect(jsonPath("$.base_image_url").value("/uploads/images/tryon/base.png"))
                .andExpect(jsonPath("$.result_image_url").doesNotExist());

        post.markCompleted("/uploads/images/tryon/result.png");
        postJpaRepository.saveAndFlush(post);
        tryonCreditJpaRepository.saveAndFlush(new TryonCredit(userId, LocalDateTime.now(), jobId));

        mockMvc.perform(get("/tryon/jobs/{jobId}", jobId).with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("completed"))
                .andExpect(jsonPath("$.base_image_url").value("/uploads/images/tryon/base.png"))
                .andExpect(jsonPath("$.result_image_url").value("/uploads/images/tryon/result.png"));

        mockMvc.perform(get("/tryon/credits").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remaining").value(4))
                .andExpect(jsonPath("$.total_daily").value(5));
    }
}
