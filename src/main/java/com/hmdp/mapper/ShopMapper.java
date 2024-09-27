package com.hmdp.mapper;

import com.hmdp.entity.Shop;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Mapper
public interface ShopMapper extends BaseMapper<Shop> {
    /**
     * 根据id获取商铺信息
     * @param id
     * @return
     */
    @Select("select * from tb_shop where id=#{id}")
    Shop getById(Long id);

    /**
     * 更新店铺
     * @param shop
     */
    void updateShopById(Shop shop);
}
