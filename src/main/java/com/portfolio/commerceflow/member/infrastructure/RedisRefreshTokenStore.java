package com.portfolio.commerceflow.member.infrastructure;

import com.portfolio.commerceflow.member.application.RefreshTokenStore;
import java.time.Duration;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RedisRefreshTokenStore implements RefreshTokenStore {

    private static final String KEY_PREFIX = "auth:refresh-token:";

    private final StringRedisTemplate redisTemplate;

    public RedisRefreshTokenStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(Long memberId, String refreshToken, Duration ttl) {
        redisTemplate.opsForValue().set(key(memberId), refreshToken, ttl);
    }

    @Override
    public Optional<String> findByMemberId(Long memberId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key(memberId)));
    }

    @Override
    public void deleteByMemberId(Long memberId) {
        redisTemplate.delete(key(memberId));
    }

    private String key(Long memberId) {
        return KEY_PREFIX + memberId;
    }
}
