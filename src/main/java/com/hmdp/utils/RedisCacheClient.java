package com.hmdp.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.entity.Shop;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.hmdp.utils.RedisConstants.LOCK_SHOP_KEY;

/**
 * Redis工具类
 */
@Component
@Slf4j
public class RedisCacheClient {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 将java对象序列为json存储在string类型的key中，key设置TTL时间
     * @param key
     * @param value
     * @param time
     * @param unit
     */
    public void setBeanToJson(String key, Object value, Long time, TimeUnit unit){
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value),time,unit);
    }

    /**
     * 将java对象序列为json存储在string类型的key中，value设置逻辑过期时间
     * @param key
     * @param value
     * @param time
     * @param unit
     */
   public void setWithLogical(String key,Object value,Long time,TimeUnit unit){
       RedisData redisData = new RedisData();
       redisData.setData(value);
       redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
       //写入redis缓存当中
       stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(redisData));
   }

    /**
     * 根据指定的key查询缓存，利用缓存空值的方式解决缓存穿透的问题
     * @param keyPrefix
     * @param id
     * @param type
     * @return
     * @param <R>
     * @param <ID>
     */
    // Function<ID,R> dbFallback 数据库操作逻辑，ID为参数类型，R为返回值类型
   public <R,ID> R queryWithPass(String keyPrefix, ID id, Class<R> type, Function<ID,R> dbFallback,Long time,TimeUnit unit) throws InterruptedException {
       String key=keyPrefix+id;
       //根据key去redis中查询
       String strJson=stringRedisTemplate.opsForValue().get(key);
       if(StrUtil.isNotBlank(strJson)){
           //redis中存有该数据，直接返回
           R r=JSONUtil.toBean(strJson,type);
           return r;
       }
       //redis中存放的是空值
       if(strJson!=null){
           //返回错误信息
           return null;
       }
       //redis中不存在该数据，从数据库中去查询，并写入redis中 此处不知道数据库中查询回来的是何种类型，所以将处理逻辑交给调用函数，因为函数知道是谁调用它
       R r=dbFallback.apply(id);
       if(r==null){
           //数据库中不存在该数据，直接给redis中写入空值，并报错
           stringRedisTemplate.opsForValue().set(key,"",time,unit);
           return null;
       }
       //数据库中存在该数据，将该数据写入缓存当中，并将该数据进行返回
       Thread.sleep(5000);
       stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(r),time,unit);
       return r;
   }

    /**
     * 根据key查询redis缓存中是否存有数据，利用互斥锁解决缓存击穿问题 包含了利用空值解决缓存穿透问题
     * 一段时间key失效，则线程重新获得互斥锁，去查询数据库并写入缓存
     * @param keyPrefix
     * @param id
     * @param type
     * @param dbFallback
     * @param time
     * @param unit
     * @return
     * @param <R>
     * @param <ID>
     */
   public <R,ID> R queryMutex(String keyPrefix,ID id,Class<R> type,Function<ID,R> dbFallback,Long time,TimeUnit unit){
       log.info("利用互斥锁解决缓存击穿问题");
       String key=keyPrefix+id;
       //查询redis中是否有该数据
       String jsonStr=stringRedisTemplate.opsForValue().get(key);
       if(StrUtil.isNotBlank(jsonStr)){
           //redis中存有该数据，直接返回
           return JSONUtil.toBean(jsonStr,type);
       }
       //判断redis命中的是否是空值
       if(jsonStr!=null){
           return null;
       }
       //redis中不存在该数据，避免此时其他线程也来访问缓存，发现缓存为空后一起去操作数据库，此时获取互斥锁，保证只有一个线程去访问数据库
       R r=null;
       //互斥锁的key与缓存数据库中的key不一样
       String lockKey=LOCK_SHOP_KEY+id;
       try {
           boolean isLock=tryLock(lockKey);
           if(!isLock){
               //为获得锁，休眠并重新访问
               Thread.sleep(1000);
               return queryMutex(keyPrefix,id,type,dbFallback, time,unit);
           }
           //获得了互斥锁,访问数据库，并把数据写入redis中
            r=dbFallback.apply(id);
           if(r==null){
               //数据库中不存在，将空值写入数据库
               stringRedisTemplate.opsForValue().set(key,"",time,unit);
               //返回错误信息
               return null;
           }
           //不为空，将实际值写入redis中
           stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(r),time,unit);
       } catch (InterruptedException e) {
           throw new RuntimeException(e);
       } finally {
           unLock(lockKey);
       }
       return r;
   }

    /**X
     * 构建互斥锁
     * @param lockKey
     * @return
     */
   public boolean tryLock(String lockKey){
       log.info("构建互斥锁");
       Boolean flag=stringRedisTemplate.opsForValue().setIfAbsent(lockKey,"1",1L,TimeUnit.MINUTES);
       return BooleanUtil.isTrue(flag);
   }

    /**
     * 释放锁
     * @param lockKy
     */
   public void unLock(String lockKy){
       stringRedisTemplate.delete(lockKy);
   }
}
