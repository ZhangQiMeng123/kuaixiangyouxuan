package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Follow;
import com.hmdp.entity.User;
import com.hmdp.mapper.FollowMapper;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IFollowService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@Slf4j
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {
    @Autowired
    private FollowMapper followMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserMapper userMapper;
    /**
     * 用户关注
     * @param folloUserId
     * @param isFollow
     */
    @Override
    public void follow(Long folloUserId, boolean isFollow) {
        //获取登录用户
        Long userId= UserHolder.getUser().getId();
        String key= RedisConstants.FOLLOW+userId;
        //判断是关注还是取消关注
        if(isFollow){
            //关注 向数据库表中插入数据
            Follow userFollow = Follow.builder()
                    .followUserId(folloUserId)
                    .userId(userId)
                    .createTime(LocalDateTime.now())
                    .build();
            followMapper.addFollowUser(userFollow);
            //修改数据库的同时，将关注的人同时也加入redis中
            stringRedisTemplate.opsForSet().add(key,folloUserId.toString());
        }else{
            //取关
            followMapper.deleteFollowUser(folloUserId);
            //将取关的用户从redis中删除
            stringRedisTemplate.opsForSet().remove(key,folloUserId.toString());
        }
    }

    /**
     * 查看是否已关注该用户
     * @param followUserId
     * @return
     */
    @Override
    public Result isFollow(Long followUserId) {
        // 1.获取登录用户
        Long userId = UserHolder.getUser().getId();
        // 2.查询是否关注 select count(*) from tb_follow where user_id = ? and follow_user_id = ?
        Integer count = query().eq("user_id", userId).eq("follow_user_id", followUserId).count();
        // 3.判断
        return Result.ok(count > 0);
    }

    /**
     * 查看共同关注
     * @param id
     * @return
     */
    @Override
    public List<User> queryCommon(Long id) {
        //博主的关注
        String key=RedisConstants.FOLLOW+id;
        //当前登录账号的用户关注
        Long userId=UserHolder.getUser().getId();
        String keyCurrentUser=RedisConstants.FOLLOW+userId;
        Set<String> set=stringRedisTemplate.opsForSet().intersect(key,keyCurrentUser);
        //获取共同关注的用户id
        List<Long> ids=set.stream().map(Long::valueOf).collect(Collectors.toList());
        if(ids==null || ids.isEmpty()){
            return Collections.emptyList();
        }
        //根据获得的id去数据库中查找用户
        StrUtil.join(",",ids);
        List<User> userList=followMapper.getCommonFollow(ids);
        return userList;
    }


}
