package com.manas.learning.springboot.caching.aspect;

import com.manas.learning.springboot.caching.config.CacheTTL;
import com.manas.learning.springboot.caching.service.CacheService;
import com.manas.learning.springboot.caching.service.CacheKeyGenerator;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Aspect
@Component
@Slf4j
public class CacheTTLProcessorAspect {

    private final CacheService cacheService;

    private final CacheKeyGenerator cacheKeyGenerator;

    private final Executor executor = Executors.newCachedThreadPool();

    @Autowired
    public CacheTTLProcessorAspect(CacheService cacheService, CacheKeyGenerator cacheKeyGenerator) {
        this.cacheService = cacheService;
        this.cacheKeyGenerator = cacheKeyGenerator;
    }

    @Around("@annotation(com.manas.learning.springboot.caching.config.CacheTTL)")
    public Object cache(ProceedingJoinPoint joinPoint) throws Throwable {

        // get the current method that is called , we need this to extract the method name
        Method method = getCurrentMethod(joinPoint);

        CacheTTL cacheTTL = method.getAnnotation(CacheTTL.class);

        // Get all the method params
        Object[] parameters = joinPoint.getArgs();

        //Generate Cache Key
        String cacheKey = generateKey(method, cacheTTL, parameters);

        // Get Value from Cache
        Object returnObject = retrieveObjectFromCache(method, cacheTTL, cacheKey);

        if (returnObject != null) return returnObject;

        // execute method to get the return object
        returnObject = joinPoint.proceed(parameters);


        putIntoCache(cacheTTL, cacheKey, returnObject);

        return returnObject;
    }

    private String generateKey(Method method, CacheTTL cacheTTL, Object[] parameters) {
        // Use the method name and params to create a key
        String generatedHashKey = cacheKeyGenerator.generateKey(method.getName(),parameters);

        String cacheName  = "".equals(cacheTTL.cacheName())? method.getName() : cacheTTL.cacheName();

        return cacheTTL.nameSpace()  + ":" + cacheName + ":" + generatedHashKey;
    }

    private void putIntoCache(CacheTTL cacheTTL, String cacheKey, Object returnObject) {
        try {
            // cache the method return object to redis cache with the key generated
            cacheService.cachePut(cacheKey, returnObject, cacheTTL.ttlMinutes());
        }catch (Exception e){
           log.warn("Unable to put into cache {}", e);
        }
    }

    private Object retrieveObjectFromCache(Method method, CacheTTL cacheTTL, String cacheKey) {
        Object returnObject;
        try {
            // call cache service to get the value for the given key if not execute  method to get the return object to be cached
            returnObject = cacheService.cacheGet(cacheKey, method.getReturnType());
            if (returnObject != null) {
                //refresh cache
                refreshCacheTTL(cacheTTL, cacheKey, returnObject);
                return returnObject;
            }
        }catch (Exception e){
            log.warn("Unable to put into cache {}", e);
        }
        return null;
    }

    private void refreshCacheTTL(CacheTTL cacheTTL, String key, Object value) {

        if(!cacheTTL.refreshTTLOnRetrieve()) {
            return;
        }

        executor.execute(new Runnable() {
            public void run() {
                cacheService.cachePutIfPresent(key, value, cacheTTL.ttlMinutes());
            }
        });
    }

    private Method getCurrentMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod();
    }
}
