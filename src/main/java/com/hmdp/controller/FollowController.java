package com.hmdp.controller;


import com.hmdp.dto.Result;
import com.hmdp.entity.User;
import com.hmdp.service.IFollowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/follow")
@Slf4j
public class FollowController {
    @Autowired
    private IFollowService followService;
    /**
     * 关注
     * @return
     */
    @PutMapping("/{id}/{isFollow}")
    public Result follow(@PathVariable Long id,@PathVariable boolean isFollow){
        log.info("用户关注接口");
        followService.follow(id,isFollow);
        return Result.ok();
    }
    //是否已关注关注
    @GetMapping("/or/not/{id}")
    public Result isFollow(@PathVariable("id") Long followUserId) {
        return followService.isFollow(followUserId);
    }
    /**
     * 共同关注接口
     * @param id
     * @return
     */
    @GetMapping("/common/{id}")
    public Result common(@PathVariable Long id){
        log.info("查看共同关注");
        List<User> userList=followService.queryCommon(id);
        return Result.ok(userList);
    }
}
