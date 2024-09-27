package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


public interface IBlogService extends IService<Blog> {
    /**
     * 根据id查看探店笔记
     * @param id
     * @return
     */
    Blog getBlogById(Long id);

    /**
     * 点赞功能
     *
     * @param id
     */
    void updateBlogLiked(Long id);

    /**
     * 查看点赞列表
     * @param id
     * @return
     */
    Result queryBlogLikes(Long id);

    /**
     * 根据用户id查看博客
     * @param current
     * @param id
     * @return
     */
    List<Blog> getBlogByUserId(Integer current, Long id);

    /**
     * 探店笔记发布
     * @param blog
     */
    void addBlog(Blog blog);

    /**
     * 分页查看邮件箱中的信息
     * @param max
     * @param offset
     * @return
     */
    Result getEmailBlog(Long max, Integer offset);
}
