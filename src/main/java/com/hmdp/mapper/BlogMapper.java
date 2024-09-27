package com.hmdp.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.entity.Blog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;


@Mapper
public interface BlogMapper extends BaseMapper<Blog> {
    @Select("select * from tb_blog where id=#{id}")
    Blog getBlogById(Long id);

    /**
     * 修改探店笔记的点赞数
     *
     * @param id
     * @return
     */
    @Update("update tb_blog set liked=liked+1 where id=#{id}")
    boolean updateBlogLikedPlus(Long id);
    @Update("update tb_blog set liked=liked-1 where id=#{id}")
    boolean updateBlogLikedMinu(Long id);
    @Select("select * from tb_blog where user_id=#{id}")
   List<Blog> getBlogByUserId(Long id);

    /**
     * 博客发布
     * @param blog1
     */

    void addBlog(Blog blog1);

    /**
     * 根据redis中的博客id去数据库中获取博客
     * @param ids
     * @return
     */
    List<Blog> getBlogList(List<Long> ids);
}
