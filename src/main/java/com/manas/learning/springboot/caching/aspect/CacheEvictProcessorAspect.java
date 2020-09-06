package com.manas.learning.springboot.caching.aspect;

import com.manas.learning.springboot.caching.config.CacheTTLEvict;
import com.manas.learning.springboot.caching.service.CacheService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.TreeSet;

@Aspect
@Component
public class CacheEvictProcessorAspect {

    private final CacheService cacheService;

    @Autowired
    public CacheEvictProcessorAspect(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @AfterReturning("@annotation(com.manas.learning.springboot.caching.config.CacheTTLEvict)")
    public void cacheEvictOnReturn(JoinPoint joinPoint) throws Throwable {
        evictCache(joinPoint);
    }

    @AfterThrowing("@annotation(com.manas.learning.springboot.caching.config.CacheTTLEvict)")
    public void cacheEvictOnException(JoinPoint joinPoint) throws Throwable {
        // get the current method that is called , we need this to extract the method name
        Method method = getCurrentMethod(joinPoint);

        CacheTTLEvict cacheTTLEvict = method.getAnnotation(CacheTTLEvict.class);

        if(cacheTTLEvict.clearOnException())
            evictCache(joinPoint);
    }

    private void evictCache(JoinPoint joinPoint) {
        // get the current method that is called , we need this to extract the method name
        Method method = getCurrentMethod(joinPoint);

        // Get all the method params
        Object[] parameters = joinPoint.getArgs();


        CacheTTLEvict cacheTTLEvict = method.getAnnotation(CacheTTLEvict.class);

        if("ALL".equals(cacheTTLEvict.cacheNames()[0])){
            String cacheKey = cacheTTLEvict.nameSpace() + ":*";
            //delete all keys in Spring Cache Name space
            Set<String> keys = cacheService.getKeys(cacheKey);
            cacheService.cacheEvict(keys);
        }else{
            Set<String> setOfKeys = new TreeSet<>();
            for (String key : cacheTTLEvict.cacheNames()){
                String cacheKey = cacheTTLEvict.nameSpace() + ":"+ key + ":*";
                setOfKeys.addAll(cacheService.getKeys(cacheKey));
                cacheService.cacheEvict(setOfKeys);
            }
        }
    }

    private Method getCurrentMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod();
    }
}
