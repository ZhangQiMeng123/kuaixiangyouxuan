package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.entity.Follow;
import com.hmdp.entity.ScollResult;
import com.hmdp.mapper.BlogMapper;
import com.hmdp.mapper.FollowMapper;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IBlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {
    @Autowired
    private BlogMapper blogMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private FollowMapper followMapper;
    /**
     * 根据id查看探店笔记
     * @param id
     * @return
     */
    @Override
    public Blog getBlogById(Long id) {
        Blog blog=blogMapper.getBlogById(id);
        return blog;
    }

    /**
     * 点赞功能
     *
     * @param id
     */
    @Override
    public void updateBlogLiked(Long id) {
        Long userId= UserHolder.getUser().getId();
        String key= RedisConstants.BLOG_LIKED_KEY+id;
        Double score=stringRedisTemplate.opsForZSet().score(key,userId.toString());
        if(score==null){
            //该用户未进行点赞 先修改数据库，点赞加1，然后将该用户id存入redis中
            blogMapper.updateBlogLikedPlus(id);
            stringRedisTemplate.opsForZSet().add(key,userId.toString(),System.currentTimeMillis());
        } else {
            //该用户已经点过赞 1.数据库中点赞数-1 2.redis中消除该用户点赞的记录
          blogMapper.updateBlogLikedMinu(id);
          stringRedisTemplate.opsForZSet().remove(key,userId.toString());
        }

    }

    /**
     * 查看点赞列表
     * @param id
     * @return
     */
    @Override
    public Result queryBlogLikes(Long id) {
         String key=RedisConstants.BLOG_LIKED_KEY+id;
         //1.查询top5的点赞用户
        Set<String> top5=stringRedisTemplate.opsForZSet().range(key,0,4);
        if(top5==null || top5.isEmpty()){
            return Result.ok(Collections.emptyList());
        }
        //2.解析出其中的用户id
        List<Long> ids = top5.stream().map(Long::valueOf).collect(Collectors.toList());
        //查询点赞列表
        String idStr = StrUtil.join(",", ids);
        List<UserDTO> userDTOList=userMapper.getUserLikedList(ids);

        return Result.ok(userDTOList);
    }

    /**
     * 根据用户id查看博客
     * @param current
     * @param id
     * @return
     */
    @Override
    public List<Blog> getBlogByUserId(Integer current, Long id) {
        List<Blog> blogList=blogMapper.getBlogByUserId(id);
        return blogList;
    }

    /**
     * 探店笔记发布
     * @param blog
     */
    @Override
    public void addBlog(Blog blog) {
        Long userId=UserHolder.getUser().getId();
        Blog blog1 = Blog.builder()
                .userId(userId)
                .shopId(blog.getShopId())
                .content(blog.getContent())
                .images(blog.getImages())
                .title(blog.getTitle())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        blogMapper.addBlog(blog1);
        //在数据库中添加博客后，将博客的id推送到粉丝的redis缓存中
        List<Follow> followList=followMapper.getFollowUsers(userId);
        for(Follow follow: followList){
            //获取粉丝id
            Long userId_=follow.getUserId();
            String key=RedisConstants.FEED_KEY+userId_;
            stringRedisTemplate.opsForZSet().add(key,blog1.getId().toString(),System.currentTimeMillis());
        }
    }

    /**
     * 分页查看邮件箱中的信息
     * @param max
     * @param offset
     * @return
     */
    @Override
    public Result getEmailBlog(Long max, Integer offset) {
        Long useId=UserHolder.getUser().getId();
        String key=RedisConstants.FEED_KEY+useId;
        //通过范围查询redis中当前用户的邮箱中博客的id
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, 0, max, offset, 2);
        //非空判断
        if(typedTuples==null || typedTuples.isEmpty()){
            return Result.ok();
        }
        //解析数据 blogId minTime offset
        List<Long> ids = new ArrayList<>(typedTuples.size());
        long minTime=0;
        int os=1;  // 5 5 4 3 2 2 1
        for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) {
            ids.add(Long.valueOf(typedTuple.getValue()));
            long time=typedTuple.getScore().longValue();
            if(time==minTime){
                os++;
            }else {
                minTime=time;
                os=1;
            }
        }
        //根据id去查询博客
        StrUtil.join(",",ids);
        List<Blog> blogList=blogMapper.getBlogList(ids);
        //将博客进行封装
        ScollResult scollResult = new ScollResult();
        scollResult.setList(blogList);
        scollResult.setOffset(os);
        scollResult.setMinTime(minTime);
        return Result.ok(scollResult);
    }
}
