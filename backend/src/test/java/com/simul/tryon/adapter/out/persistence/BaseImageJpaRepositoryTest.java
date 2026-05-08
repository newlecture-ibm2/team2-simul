package com.simul.tryon.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.simul.common.config.JpaConfig;
import com.simul.tryon.domain.model.BaseImage;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(JpaConfig.class)
class BaseImageJpaRepositoryTest {

    @Autowired
    private BaseImageJpaRepository baseImageJpaRepository;

    @Test
    void findAllByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc_filtersByUser() {
        UUID userA = UUID.randomUUID();
        UUID userB = UUID.randomUUID();

        baseImageJpaRepository.save(new BaseImage(userA, "/uploads/images/tryon/a.jpg"));
        baseImageJpaRepository.save(new BaseImage(userB, "/uploads/images/tryon/b.jpg"));
        baseImageJpaRepository.save(new BaseImage(userA, "/uploads/images/tryon/c.jpg"));

        List<BaseImage> result =
                baseImageJpaRepository.findAllByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userA);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(it -> it.getUserId().equals(userA));
    }
}
