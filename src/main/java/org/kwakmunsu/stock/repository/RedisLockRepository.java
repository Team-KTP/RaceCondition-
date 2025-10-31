package org.kwakmunsu.stock.repository;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RedisLockRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public Boolean lock(Long lockKey) {
        return redisTemplate
                .opsForValue()
                .setIfAbsent(generateLockKey(lockKey), "lock", Duration.ofSeconds(3_000));
    }

    public Boolean unlock(Long lockKey) {
        return redisTemplate.delete(generateLockKey(lockKey));
    }

    private String generateLockKey(Long lockKey) {
        return lockKey.toString();
    }

}
