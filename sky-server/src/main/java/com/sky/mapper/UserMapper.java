package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {

    /**
     * 根据openid查询用户
     * @param openid 微信用户openid
     * @return 用户信息
     */
    @Select("select * from sky_take_out.user where openid = #{openid}")
    User getByOpenid(String openid);

     /**
     * 插入用户
     * @param user 用户信息
     */
    void insert(User user);

     /**
     * 根据用户id查询用户
     * @param userId 用户id
     * @return 用户信息
     */
    @Select("select * from sky_take_out.user where id = #{id}")
    User getById(Long userId);

    /**
     * 根据map统计用户数量
     * @param map 查询条件
     * @return 用户数量
     */
    Integer countByMap(Map map);
}
