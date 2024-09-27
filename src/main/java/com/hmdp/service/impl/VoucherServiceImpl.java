package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.Voucher;
import com.hmdp.mapper.VoucherMapper;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherService;
import com.hmdp.utils.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;


@Service
@Slf4j
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Autowired
    private VoucherMapper voucherMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    /**
     * 添加普通优惠卷
     * @param voucher
     */
    @Override
    public void addOrdinaryVoucher(Voucher voucher) {
       log.info("新增普通优惠卷");
       //将优惠卷保存到数据库
        voucher.setCreateTime(LocalDateTime.now());
        voucher.setUpdateTime(LocalDateTime.now());
        voucherMapper.addOrdinaryVoucher(voucher);
    }

    /**
     * 新增秒杀卷
     * @param voucher
     */
    @Override
    @Transactional
    public void addSeckillVoucher(Voucher voucher) {
        // 秒杀卷也是优惠券，先将秒杀卷添加到优惠卷表中
        voucher.setUpdateTime(LocalDateTime.now());
        voucher.setCreateTime(LocalDateTime.now());
        voucherMapper.addOrdinaryVoucher(voucher);
        // 将秒杀卷添加到秒杀卷表中
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        BeanUtils.copyProperties(voucher,seckillVoucher);
        seckillVoucher.setVoucherId(voucher.getId());
        voucherMapper.addSeckillVoucher(seckillVoucher);
        //同时将秒杀卷库存信息保存到redis中
        stringRedisTemplate.opsForValue().set(RedisConstants.SECKILL_STOCK_KEY+voucher.getId(),voucher.getStock().toString());
    }

    /**
     * 优惠卷进行显示
     * @param shopId
     * @return
     */
    @Override
    public List<Voucher> queryVoucherOfShop(Long shopId) {
        List<Voucher> voucherList=voucherMapper.getVoucherByShopId(shopId);
        return voucherList;
    }
}
