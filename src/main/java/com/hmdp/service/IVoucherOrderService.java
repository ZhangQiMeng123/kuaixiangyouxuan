package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {
    /**
     * 实现秒杀下单，判断代金券信息
     *
     * @param voucherId
     * @return
     */
    Result getSeckillVoucherDetail(Long voucherId);


    Result createVoucherOrder(Long voucherId, Long userId);
}
