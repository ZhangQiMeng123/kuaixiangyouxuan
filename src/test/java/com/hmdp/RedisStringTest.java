package com.hmdp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdp.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.*;

import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RedisStringTest {
    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testString() {
        // set get setex setnx
        redisTemplate.opsForValue().set("", "小明");
        redisTemplate.opsForValue().set("age",21);
        //获取string数据
        Object name=redisTemplate.opsForValue().get("name");
        System.out.println("name="+name);
//        redisTemplate.opsForValue().set("code", "1234", 3, TimeUnit.MINUTES); //key,value,超时时间，时间单位
//        redisTemplate.opsForValue().setIfAbsent("lock", "1"); // redies中value可以给任意对象，最后都会被自动转成string类型
//        redisTemplate.opsForValue().setIfAbsent("lock", 2);

    }

    /**
     * 自定义redisTemplate序列化
     */
    @Test
    void test(){
        User user = new User();
        user.setNickName("虎哥");
        user.setPhone("13952347063");
        redisTemplate.opsForValue().set("User",user);
    }

    /**
     * 手动序列化
     */

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    //序列化工具
    private static final  ObjectMapper mapper=new ObjectMapper();
    @Test
    void test01() throws JsonProcessingException {
        //创建对象
        User user = new User();
        user.setNickName("虎哥");
        user.setPhone("139523470631");
        //手动序列化
        String json=mapper.writeValueAsString(user);
        //写入数据
        stringRedisTemplate.opsForValue().set("User:200",json);

    }

}
