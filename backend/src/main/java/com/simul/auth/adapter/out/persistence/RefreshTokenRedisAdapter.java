package com.simul.auth.adapter.out.persistence;

import com.simul.auth.application.port.out.RefreshTokenPort;
import com.simul.auth.domain.model.RefreshToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 리프레시 토큰 Redis 어댑터 (Output Port 구현체)
 *
 * - RefreshTokenPort 인터페이스를 구현하여 서비스 레이어에 제공
 * - 내부적으로 RefreshTokenRedisRepository를 사용하여 Redis와 통신
 * - 도메인 모델 ↔ Redis 엔티티 간 변환(매핑) 담당
 */
@Component
public class RefreshTokenRedisAdapter implements RefreshTokenPort {

    private final RefreshTokenRedisRepository redisRepository;

    public RefreshTokenRedisAdapter(RefreshTokenRedisRepository redisRepository) {
        this.redisRepository = redisRepository;
    }

    @Override
    public void save(RefreshToken refreshToken) {
        RefreshTokenRedisEntity entity = new RefreshTokenRedisEntity(
            refreshToken.getToken(),
            refreshToken.getUserId(),
            refreshToken.getTimeToLive()
        );
        redisRepository.save(entity);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return redisRepository.findById(token)
            .map(entity -> RefreshToken.builder()
                .token(entity.getToken())
                .userId(UUID.fromString(entity.getUserId()))
                .timeToLive(entity.getTimeToLive())
                .build()
            );
    }

    @Override
    public void deleteByToken(String token) {
        redisRepository.deleteById(token);
    }

    @Override
    public void deleteAllByUserId(UUID userId) {
        List<RefreshTokenRedisEntity> tokens = redisRepository.findByUserId(userId.toString());
        redisRepository.deleteAll(tokens);
    }
}
