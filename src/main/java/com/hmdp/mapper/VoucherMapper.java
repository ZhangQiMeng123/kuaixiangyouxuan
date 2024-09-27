package com.hmdp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.Voucher;
import org.apache.ibatis.annotations.*;

import java.util.List;


@Mapper
public interface VoucherMapper extends BaseMapper<Voucher> {


    /**
     * 添加普通优惠卷
     * @param voucher
     */
    void addOrdinaryVoucher(Voucher voucher);

    /**
     * 添加秒杀卷
     * @param seckillVoucher
     */
    @Insert("insert into tb_seckill_voucher(voucher_id, stock, create_time, begin_time, end_time, update_time) value " +
            "(#{voucherId},#{stock},#{createTime},#{beginTime},#{endTime},#{updateTime})")
    void addSeckillVoucher(SeckillVoucher seckillVoucher);

    /**
     * 优惠卷进行显示
     * @param shopId
     * @return
     */
   List<Voucher> getVoucherByShopId(Long shopId);

    /**
     * 根据代金券id获取代金卷
     * @param voucherId
     * @return
     */
    @Select("select * from tb_seckill_voucher where voucher_id=#{voucherId}")
    Voucher getVoucherById(Long voucherId);

    /**
     * 购买代金卷
     * @param voucherId
     */
    @Update("update tb_seckill_voucher set stock=stock-1 where voucher_id=#{voucherId} and stock>0")
    void buyVoucher(Long voucherId);
}
