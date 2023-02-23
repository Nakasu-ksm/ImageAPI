package com.animetrace.animeimage.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service("RedisService")
public class RedisService {
    @Resource
    private RedisTemplate redisTemplate;
    @Cacheable(cacheNames = "RedisCache", key="#key")
    public Map<String, Object> getCache(String key){

        Map<String,Object> map = new HashMap<>();
        map.put("exist", false);
        map.put("object",null);
        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            return map;
        }
        map.put("exist",true);
        map.put("object",redisTemplate.opsForValue().get(key));
        return map;
    }
}
