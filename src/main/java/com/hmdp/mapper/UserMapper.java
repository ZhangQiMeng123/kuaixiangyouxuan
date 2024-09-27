package com.hmdp.mapper;

import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface UserMapper extends BaseMapper<User> {
    /**
     * 查询博客点赞列表
     * @param ids
     * @return
     */
    List<UserDTO> getUserLikedList(List<Long> ids);
    @Select("select * from tb_user where id=#{id}")
    User getUserById(Long id);
}
