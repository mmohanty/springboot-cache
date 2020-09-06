package com.manas.learning.springboot.caching.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

@Component
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheTTL {

    String value() default "";

    String cacheName() default "";

    int ttlMinutes() default 1;

    String nameSpace() default "cache";

    boolean refreshTTLOnRetrieve() default  true;
}