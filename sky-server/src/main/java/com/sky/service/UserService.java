package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

public interface UserService {

    /**
     * 微信登录
     * @param userLoginDTO 用户登录DTO
     * @return 用户登录VO
     */
    User wxLogin(UserLoginDTO userLoginDTO);
}
