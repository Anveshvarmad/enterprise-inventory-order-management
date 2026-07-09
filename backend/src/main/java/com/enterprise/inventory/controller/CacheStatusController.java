package com.enterprise.inventory.controller;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class CacheStatusController {

    private final StringRedisTemplate redisTemplate;
    private final RedisConnectionFactory redisConnectionFactory;

    public CacheStatusController(
            StringRedisTemplate redisTemplate,
            RedisConnectionFactory redisConnectionFactory
    ) {
        this.redisTemplate = redisTemplate;
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @GetMapping("/api/cache/status")
    public Map<String, Object> cacheStatus() {
        String key = "cache-health-check";
        String value = "UP-" + Instant.now();

        redisTemplate.opsForValue().set(key, value);
        String cachedValue = redisTemplate.opsForValue().get(key);

        String pingResponse;

        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            pingResponse = connection.ping();
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "CONNECTED");
        response.put("cache", "Redis");
        response.put("ping", pingResponse);
        response.put("testKey", key);
        response.put("testValue", cachedValue);

        return response;
    }
}
