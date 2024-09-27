package com.hmdp.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.entity.User;
import com.hmdp.service.IBlogService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.SystemConstants;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


@RestController
@RequestMapping("/blog")
@Slf4j
public class BlogController {

    @Resource
    private IBlogService blogService;
    @Resource
    private IUserService userService;

    /**
     * 探店笔记发布
     * @param blog
     * @return
     */
    @PostMapping
    public Result saveBlog(@RequestBody Blog blog) {
        log.info("探店笔记发布：{}",blog);
        blogService.addBlog(blog);
        return Result.ok();
    }

    /**
     * 根据id查看探店笔记
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result getBlogById( @PathVariable("id") Long id){
        log.info("查看探店笔记:{}",id);
        Blog blog=blogService.getBlogById(id);
        return Result.ok(blog);
    }

    /**
     * 探店笔记点赞实现接口
     * @param id
     * @return
     */
    @PutMapping("/like/{id}")
    public Result likeBlog(@PathVariable("id") Long id) {
        log.info("点赞功能实现:{}",id);
        // 修改点赞数
        blogService.updateBlogLiked(id);
        return Result.ok();
    }


    /**
     * 点赞列表查看
     * @param
     * @return
     */
    @GetMapping("/likes/{id}")
    public Result queryBlogLikes(@PathVariable("id") Long id){
        log.info("点赞列表查看");
        return blogService.queryBlogLikes(id);
    }

    @GetMapping("/of/me")
    public Result queryMyBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 获取登录用户
        UserDTO userDTO = UserHolder.getUser();
        // 根据用户查询
        Page<Blog> page = blogService.query()
                .eq("user_id", userDTO.getId()).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        return Result.ok(records);
    }

    @GetMapping("/hot")
    public Result queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 根据用户查询
        Page<Blog> page = blogService.query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        // 查询用户
        records.forEach(blog ->{
            Long userId = blog.getUserId();
            User user = userService.getById(userId);
            blog.setName(user.getNickName());
            blog.setIcon(user.getIcon());
        });
        return Result.ok(records);
    }

    /**
     * 根据id查看个人博客
     * @param current
     * @param id
     * @return
     */
    @GetMapping("/of/user")
    public Result getBlogById(@RequestParam(value = "current",defaultValue = "1") Integer current,
                              @RequestParam("id") Long id){
        log.info("根据用户id查看博客");
        List<Blog> blogList=blogService.getBlogByUserId(current,id);
        return Result.ok(blogList);
    }

    /**
     * 分页查看收邮件箱中的信息
     * @param max
     * @param offset
     * @return
     */
    @GetMapping("/of/follow")
    public Result queryBlogOfFollow(
            @RequestParam("lastId") Long max,@RequestParam(value = "offset",defaultValue = "0") Integer offset){
            log.info("分页查询收件箱中的信息");
            return blogService.getEmailBlog(max,offset);
    }

}
