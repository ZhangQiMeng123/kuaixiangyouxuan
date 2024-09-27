package com.hmdp.service;


import com.hmdp.entity.Voucher;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;


public interface IVoucherService extends IService<Voucher> {



    /**
     * 添加普通优惠卷
     * @param voucher
     */
    void addOrdinaryVoucher(Voucher voucher);

    /**
     * 新增秒杀卷
     * @param voucher
     */
    void addSeckillVoucher(Voucher voucher);

    /**
     * 优惠卷进行显示
     * @param shopId
     * @return
     */
    List<Voucher> queryVoucherOfShop(Long shopId);
}
