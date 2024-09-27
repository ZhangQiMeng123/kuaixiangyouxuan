package com.hmdp.mapper;

import com.hmdp.entity.Follow;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmdp.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface FollowMapper extends BaseMapper<Follow> {
    @Insert("insert into tb_follow(user_id, follow_user_id, create_time) value (#{userId},#{followUserId},#{createTime})")
    void addFollowUser(Follow userFollow);
    @Delete("delete from tb_follow where follow_user_id=#{followUserId}")
    void deleteFollowUser(Long followUserId);

    /**
     * 查看共同关注
     * @param ids
     * @return
     */
    List<User> getCommonFollow(List<Long> ids);

    /**
     * 查询当前用户的粉丝
     * @param userId
     * @return
     */
    @Select("SELECT * FROM tb_follow WHERE follow_user_id=#{userId}")
    List<Follow> getFollowUsers(Long userId);
}
