package com.manas.learning.springboot.caching.config;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Component
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheTTLEvict {

    String[] cacheNames() default "ALL";

    String nameSpace() default "cache";

    boolean clearOnException() default false;
}