package com.seckill.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.seckill.common.entity.PageInfo;
import com.seckill.user.entity.User;
import com.seckill.user.vo.TokenVO;
import com.seckill.user.vo.UserVO;

/**
 * 用户Service接口
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     */
    UserVO register(User user);

    /**
     * 用户登录
     */
    TokenVO login(String username, String password);

    /**
     * 获取当前登录用户信息
     */
    UserVO getCurrentUser(Long userId);

    /**
     * 分页查询用户列表（管理员）
     */
    PageInfo<UserVO> getUserPage(Integer pageNum, Integer pageSize, String username);

    /**
     * 禁用/启用用户（管理员）
     */
    void updateUserStatus(Long id, Integer status);
}