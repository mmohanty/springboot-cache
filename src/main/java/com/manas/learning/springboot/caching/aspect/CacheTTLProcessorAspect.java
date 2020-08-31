package com.manas.learning.springboot.caching.aspect;

import com.manas.learning.springboot.caching.config.CacheTTL;
import com.manas.learning.springboot.caching.service.CacheService;
import com.manas.learning.springboot.caching.util.CacheKeyGenerator;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@EnableAspectJAutoProxy
@Component
public class CacheTTLProcessorAspect {

    @Autowired
    private CacheService cacheService;

    @Around("@annotation(CacheTTL)")
    public Object cacheTTL(ProceedingJoinPoint joinPoint) throws Throwable {

        // get the current method that is called , we need this to extract the method name
        Method method = getCurrentMethod(joinPoint);

        // Get all the method params
        Object[] parameters = joinPoint.getArgs();

        // Use the method name and params to create a key
        String key = CacheKeyGenerator.generateKey(method.getName(),parameters);

        // call cache service to get the value for the given key if not execute  method to get the return object to be cached
        Object returnObject = cacheService.cacheGet(key, method.getReturnType());
        if (returnObject != null)
            return returnObject;

        // execute method to get the return object
        returnObject = joinPoint.proceed(parameters);

        CacheTTL cacheTTL = method.getAnnotation(CacheTTL.class);

        // cache the method return object to redis cache with the key generated
        cacheService.cachePut(key, returnObject, cacheTTL.ttlMinutes());

        return returnObject;
    }

    private Method getCurrentMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod();
    }
}
