package com.manas.learning.springboot.caching.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class CacheService {

    @Autowired
    private RedisTemplate<String, Object> template;

    @Value("${spring.redis.cache.enabled:true}")
    private boolean cacheEnabled;

    /**
     * Clear redis cache, clear all keys
     */
    public void clearAllCaches() {
        template.execute((RedisCallback<Object>) connection -> {
            connection.flushAll();
            return null;
        });
    }


    /**
     * Add key and value to redis cache with ttlMinutes as the key expiration in minutes
     *
     * @param key
     * @param toBeCached
     * @param ttlMinutes
     */
    public void cachePut(String key, Object toBeCached, long ttlMinutes) {
        if (!cacheEnabled)
            return;

        template.opsForValue().set(key, toBeCached, ttlMinutes, TimeUnit.MINUTES);
    }

    /**
     * Add key and value to redis cache with no expiration of key
     *
     * @param key
     * @param toBeCached
     */
    public void cachePut(String key, Object toBeCached) {
        if (!cacheEnabled)
            return;

        if (toBeCached == null)
            return;

        cachePut(key, toBeCached, -1);
    }

    /**
     * Get the value for the given key from redis cache
     *
     * @param key
     * @param type
     * @return
     */
    public <T> T cacheGet(String key, Class<T> type) {
        if (!cacheEnabled)
            return null;

        return (T) template.opsForValue().get(key);

    }
}