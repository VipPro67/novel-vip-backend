package com.novel.vippro.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RedisViewStatService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String VIEW_DELTA_KEY_PREFIX = "novel:view_delta:";
    private static final String PENDING_SYNC_KEY = "novels:pending_view_sync";

    public void incrementViewDelta(UUID novelId) {
        String key = VIEW_DELTA_KEY_PREFIX + novelId.toString();
        redisTemplate.opsForValue().increment(key);
        redisTemplate.opsForSet().add(PENDING_SYNC_KEY, novelId.toString());
    }
    public Set<UUID> getPendingSyncNovels() {
        Set<Object> members = redisTemplate.opsForSet().members(PENDING_SYNC_KEY);
        if (members == null) {
            return Set.of();
        }
        return members.stream()
                .map(obj -> UUID.fromString(obj.toString()))
                .collect(Collectors.toSet());
    }
    public Long resetViewDelta(UUID novelId) {
        String key = VIEW_DELTA_KEY_PREFIX + novelId.toString();
        redisTemplate.opsForSet().remove(PENDING_SYNC_KEY, novelId.toString());

        Object val = redisTemplate.opsForValue().getAndSet(key, 0);

        if (val instanceof Number) {
            return ((Number) val).longValue();
        } else if (val instanceof String) {
            try {
                return Long.parseLong((String) val);
            } catch (NumberFormatException e) {
                return 0L;
            }
        }
        return 0L;
    }
}
