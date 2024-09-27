package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.Follow;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.entity.User;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IFollowService extends IService<Follow> {
    /**
     * 用户关注
     * @param id
     * @param isFollow
     */
    void follow(Long id, boolean isFollow);

    /**
     * 查看共同关注
     * @param id
     * @return
     */
    List<User> queryCommon(Long id);

    /**
     * 查看是否已关注该用户
     * @param followUserId
     * @return
     */
    Result isFollow(Long followUserId);
}
