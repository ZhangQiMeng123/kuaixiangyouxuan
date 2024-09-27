package com.hmdp.mapper;

import com.hmdp.entity.VoucherOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VoucherOrderMapper extends BaseMapper<VoucherOrder> {
    /**
     * 增加订单
     * @param voucherOrder
     */
    @Insert("insert into tb_voucher_order(id, user_id, voucher_id,create_time) value (#{id},#{userId},#{voucherId},#{createTime})")
    void addOrder(VoucherOrder voucherOrder);

    /**
     * 一人一单逻辑，判断用户是否下过单
     * @param userId
     * @param voucherId
     * @return
     */
    @Select("select count(user_id) from tb_voucher_order where user_id=#{userId} and voucher_id=#{voucherId} ")
    int getOrderCountByUserId(long userId, Long voucherId);
}
