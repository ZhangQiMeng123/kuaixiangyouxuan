package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ShopTypeMapper shopTypeMapper;
    /**
     * 获取店铺类型列表
     *
     * @return
     */
    @Override
    public List<ShopType> getShopTypeList() {
        String key= RedisConstants.SHOP_TYPE;
        String jsonStr=stringRedisTemplate.opsForValue().get(key);
        if(StrUtil.isNotBlank(jsonStr)){
            List<ShopType> shopTypes=JSONUtil.toList(jsonStr,ShopType.class);
            return shopTypes;
        }
        //不存在，查询数据库
        List<ShopType> shopTypeList=shopTypeMapper.getShopType();
        if(shopTypeList==null){
            return (List<ShopType>) Result.fail("商铺类型不存在！");
        }
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shopTypeList));
        return shopTypeList;
    }
}
