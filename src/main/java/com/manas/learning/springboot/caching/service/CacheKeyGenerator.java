package com.manas.learning.springboot.caching.service;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class CacheKeyGenerator {

    /**
     * Append the method name , param to an array and create a deepHashCode of the array as redis cache key
     * @param methodName
     * @param params
     * @return
     */
    public String generateKey(String methodName , Object... params) {
        if (params.length == 0) {
            return Integer.toString(methodName.hashCode());
        }
        Object[] paramList = new Object[params.length+1];
        paramList[0] = methodName;
        System.arraycopy(params, 0, paramList, 1, params.length);
        int hashCode = Arrays.deepHashCode(paramList);
        return Integer.toString(hashCode);
    }
}
