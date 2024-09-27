package com.hmdp.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 生成全局ID的生成工具
 */
@Component
public class RedisIdWorker {
    /**
     * 开始时间戳
     */
    private static long BEGIN_TIMESTAMP=1718064000L;

    /**
     * 序列号的位数
     *
     */
    private static final int COUNT_BITS=32;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //生成全局唯一ID
    public long nextId(String keyPrefix){
        //1.生成时间戳
        LocalDateTime now=LocalDateTime.now();
        long newSeconds=now.toEpochSecond(ZoneOffset.UTC);
        long timeStamp=newSeconds-BEGIN_TIMESTAMP;
        //2.生成序列号
         //2.1获取当前日期，精确到天
        String data=now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        //2.2 自增长
        long count=stringRedisTemplate.opsForValue().increment("icr:"+keyPrefix+":"+data);
        //3.时间戳与序列号进行拼接
        return timeStamp<<COUNT_BITS | count;
    }


}
