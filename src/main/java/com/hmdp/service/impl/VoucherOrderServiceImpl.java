package com.hmdp.service.impl;

import com.hmdp.dto.Result;
import com.hmdp.entity.Voucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherMapper;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.SimpleRedisLock;
import com.hmdp.utils.UserHolder;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;


@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {
    @Autowired
    private VoucherMapper voucherMapper;
    @Autowired
    private RedisIdWorker redisIdWorker;
    @Autowired
    private VoucherOrderMapper voucherOrderMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    /**
     * 定义lua脚本
     */
     private final  static DefaultRedisScript<Long> ORDERJUDGE_SCRIPT;
     static {
       ORDERJUDGE_SCRIPT=  new DefaultRedisScript<>();
       ORDERJUDGE_SCRIPT.setLocation(new ClassPathResource("OrderJudge.lua"));
       ORDERJUDGE_SCRIPT.setResultType(Long.class);
     }
    /**
     * 实现秒杀下单，判断代金券信息
     *
     * @param voucherId
     * @return
     */
    @Override
    public Result getSeckillVoucherDetail(Long voucherId) {
        /*
        //1.根据代金券id获取代金券信息，判断代金券是否可以购买
        Voucher voucher=voucherMapper.getVoucherById(voucherId);
        LocalDateTime now=LocalDateTime.now();
        //判断时间是否开始或结束
        if(voucher.getBeginTime().isAfter(now)) return Result.fail("活动还未开始!");
        if (voucher.getEndTime().isBefore(now)) return Result.fail("活动已经结束!");
        //判断库存是否充足
        if(voucher.getStock()<1) return Result.fail("库存不足!");
        //判断该用户是否下过单
        Long userId= UserHolder.getUser().getId();
        //创建分布式锁
        SimpleRedisLock lock = new SimpleRedisLock("order:"+userId,stringRedisTemplate);
        boolean isLock=lock.tryLock(1200);
        if(!isLock){
            return Result.fail("不允许重复下单!");
        }
        try {
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder( voucherId, userId);
        } catch (IllegalStateException e) {
            throw new RuntimeException(e);
        }finally {
            lock.unLock();
        }

         */
        /**
         * 通过lua表达式来判断用户是否有下单资格
         */

        Long userId=UserHolder.getUser().getId();
        Long orderId=redisIdWorker.nextId("order");
        //执行lua脚本
        Long result=stringRedisTemplate.execute(ORDERJUDGE_SCRIPT,
                Collections.emptyList(),voucherId.toString(),userId.toString(),String.valueOf(orderId));
        int r=result.intValue();
        //判断结果是否为0
        if(r!=0){
            return Result.fail(r==1?"库存不足":"重复下单");
        }
        //TODO 保存阻塞队列
        //3.返回订单id
        return Result.ok(orderId);
    }
    @Transactional
    public  Result createVoucherOrder(Long voucherId,Long userId){


            int count = voucherOrderMapper.getOrderCountByUserId(userId, voucherId);
            if (count != 0) {
                return Result.fail("用户已经下过单!");
            }
            //购买代金券 库存减一
            voucherMapper.buyVoucher(voucherId);
            //创建订单
            VoucherOrder voucherOrder = new VoucherOrder();
            long orderId = redisIdWorker.nextId("order");
            voucherOrder.setUserId(userId);
            voucherOrder.setId(orderId);
            voucherOrder.setVoucherId(voucherId);
            voucherOrder.setCreateTime(LocalDateTime.now());
            voucherOrderMapper.addOrder(voucherOrder);
            return Result.ok(orderId);
        }
}
