package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisCacheClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
       @Resource
       private StringRedisTemplate stringRedisTemplate;
       @Autowired
       private ShopMapper shopMapper;

       //创建Redis工具类
       @Autowired
       private RedisCacheClient redisCacheClient;
    /**
     * 查询商户信息
     * @param id
     * @return
     */
    @Override
    public Result queryById(Long id) throws InterruptedException {
        //根据key去查询redis中是否存在数据，利用空值避免缓存穿透问题
        Shop shop = redisCacheClient.queryMutex(CACHE_SHOP_KEY,id,Shop.class,shopMapper::getById,1L,TimeUnit.MINUTES);
//        //判断redis中是否存在
//        if(StrUtil.isNotBlank(shopJson)){
//            //存在。直接返回
//            Shop shop= JSONUtil.toBean(shopJson,Shop.class);
//            return Result.ok(shop);
//        }
//        //判断是否命中的是空值
//        if(shopJson != null){
//            return Result.fail("店铺信息不存在！");
//        }
//        //不存在的话，查询数据库，并将查询到的数据写入到redis缓存当中
//        //实现缓存重构
//        Shop shop=null;
//        String lockKey="lock:shop:"+id;
//        try {
//            boolean isLock=tryLock(lockKey);
//            if(!isLock){
//                //没有获得互斥锁则休眠一段时间，并重新查找
//                Thread.sleep(50);
//                return queryById(id);
//            }
//            //获得互斥锁，则查询数据库，并将数据库中的信息写到缓存当中
//            shop=shopMapper.getById(id);
//            if(shop==null) {
//                //将空值写入redis中
//                stringRedisTemplate.opsForValue().set(key,"",RedisConstants.CACHE_NULL_TTL,TimeUnit.MINUTES);
//                return Result.fail("店铺不存在！");
//            }
//            //不空，将实际值写入redis中
//            stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shop),2, TimeUnit.MINUTES);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }finally {
//            //释放锁
//            unlock(lockKey);
//        }
        if(shop==null){
            return Result.fail("店铺不存在");
        }
        return Result.ok(shop);

    }

    /**
     * 更新商户信息 要求先修改数据库，再删除缓存，后续用户使用时，再将数据库信息写入缓存
     * @param shop
     */
    @Override
    public void updateShopById(Shop shop) {
        log.info("更新数据库，删除redis缓存");
        shop.setUpdateTime(LocalDateTime.now());
        //更新数据库
        shopMapper.updateShopById(shop);
        //删除redis缓存
        String key= CACHE_SHOP_KEY+shop.getId();
        stringRedisTemplate.delete(key);
    }

    /**
     * 构建互斥锁
     * @param key
     * @return
     */
    private boolean tryLock(String key){
        log.info("构建互斥锁");
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    /**
     * 释放锁
     * @param key
     */
    private void unlock(String key){
        stringRedisTemplate.delete(key);
    }
}


