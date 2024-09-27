package com.hmdp.controller;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.*;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;


@RestController
public class RedisStringTest {
    @Autowired
    private RedisTemplate redisTemplate;

  
    public void testString() {
        // set get setex setnx
        redisTemplate.opsForValue().set("name", "小明");
        String city = (String) redisTemplate.opsForValue().get("name");
        System.out.println(city);
        redisTemplate.opsForValue().set("code", "1234", 3, TimeUnit.MINUTES); //key,value,超时时间，时间单位
        redisTemplate.opsForValue().setIfAbsent("lock", "1"); // redies中value可以给任意对象，最后都会被自动转成string类型
        redisTemplate.opsForValue().setIfAbsent("lock", "2");
    }
    
    public void testRedisTemplate(){
        System.out.println(redisTemplate);
        //string数据操作
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //hash类型的数据操作
        HashOperations hashOperations = redisTemplate.opsForHash();
        //list类型的数据操作
        ListOperations listOperations = redisTemplate.opsForList();
        //set类型数据操作
        SetOperations setOperations = redisTemplate.opsForSet();
        //zset类型数据操作
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
    }
}
