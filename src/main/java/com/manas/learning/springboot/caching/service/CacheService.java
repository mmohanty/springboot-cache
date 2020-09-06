package com.manas.learning.springboot.caching.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class CacheService {

    private final RedisTemplate<String, Object> template;

    @Value("${spring.redis.cache.enabled:true}")
    private boolean cacheEnabled;

    @Autowired
    public CacheService(@Qualifier("SpringBootCacheRedisTemplate") RedisTemplate<String, Object> template) {
        this.template = template;
    }

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
    public void cachePut(final String key, final Object toBeCached, long ttlMinutes) {
        if (!cacheEnabled)
            return;

        template.opsForValue().set(key, toBeCached, ttlMinutes, TimeUnit.MINUTES);
    }


    /**
     * Add key and value to redis cache If present with ttlMinutes as the key expiration in minutes
     *
     * @param key
     * @param toBeCached
     * @param ttlMinutes
     */
    public void cachePutIfPresent(final String key, final Object toBeCached, long ttlMinutes) {
        if (!cacheEnabled)
            return;

        template.opsForValue().setIfPresent(key, toBeCached, ttlMinutes, TimeUnit.MINUTES);
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
    public <T> T cacheGet(final String key, final Class<T> type) {
        if (!cacheEnabled)
            return null;

        return (T) template.opsForValue().get(key);

    }

    /**
     * Delete the value from Redis for given keys.
     *
     * @param keys Keys to be deleted
     * @return
     */
    public long cacheEvict(final Set<String> keys) {
        if (!cacheEnabled)
            return -1;

        return template.delete(keys);

    }

    /**
     * Get All keys which matches the pattern.
     *
     * @param pattern Key Patterns to be searched for
     * @return
     */

    public Set<String> getKeys(final String pattern) {
        if (!cacheEnabled)
            return null;

        return template.keys(pattern);

    }
}