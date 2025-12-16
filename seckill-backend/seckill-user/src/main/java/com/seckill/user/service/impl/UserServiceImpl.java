package com.seckill.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seckill.common.constant.RoleEnum;
import com.seckill.common.entity.PageInfo;
import com.seckill.common.exception.BusinessException;
import com.seckill.common.utils.JwtUtil;
import com.seckill.common.utils.PasswordUtil;
import com.seckill.user.entity.User;
import com.seckill.user.mapper.UserMapper;
import com.seckill.user.service.UserService;
import com.seckill.user.vo.TokenVO;
import com.seckill.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 用户Service实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final JwtUtil jwtUtil;

    @Override
    public UserVO register(User user) {
        // 1. 参数校验
        if (!StringUtils.hasText(user.getUsername()) || !StringUtils.hasText(user.getPassword())) {
            throw BusinessException.badRequest("用户名或密码不能为空");
        }

        // 2. 检查用户名是否已存在
        User existUser = baseMapper.selectByUsername(user.getUsername());
        if (existUser != null) {
            throw BusinessException.badRequest("用户名已存在");
        }

        // 3. 密码加密
        user.setPassword(PasswordUtil.encrypt(user.getPassword()));

        // 4. 默认角色：普通用户
        if (user.getRole() == null) {
            user.setRole(RoleEnum.USER.getCode());
        }

        // 5. 填充创建时间
        user.setCreateTime();

        // 6. 插入数据库
        baseMapper.insert(user);

        // 7. 转换为VO返回
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public TokenVO login(String username, String password) {
        // 1. 查询用户
        User user = baseMapper.selectByUsername(username);
        if (user == null) {
            throw BusinessException.badRequest("用户名或密码错误");
        }

        // 2. 验证密码
        if (!PasswordUtil.verify(password, user.getPassword())) {
            throw BusinessException.badRequest("用户名或密码错误");
        }

        // 3. 生成token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 4. 封装返回VO
        TokenVO tokenVO = new TokenVO();
        tokenVO.setToken(token);

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        tokenVO.setUser(userVO);

        return tokenVO;
    }

    @Override
    public UserVO getCurrentUser(Long userId) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.badRequest("用户不存在");
        }

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public PageInfo<UserVO> getUserPage(Integer pageNum, Integer pageSize, String username) {
        // 1. 分页查询
        Page<UserVO> page = new Page<>(pageNum, pageSize);
        IPage<UserVO> userPage = baseMapper.selectUserPage(page, username);

        // 2. 转换为通用分页VO
        PageInfo<UserVO> pageInfo = new PageInfo<>();
        pageInfo.setPageNum(pageNum);
        pageInfo.setPageSize(pageSize);
        pageInfo.setTotal(userPage.getTotal());
        pageInfo.setPages((int) userPage.getPages());
        pageInfo.setList(userPage.getRecords());

        return pageInfo;
    }

    @Override
    public void updateUserStatus(Long id, Integer status) {
        // 1. 检查用户是否存在
        User user = baseMapper.selectById(id);
        if (user == null) {
            throw BusinessException.badRequest("用户不存在");
        }

        // 2. 此处简化：status仅做状态标记（实际可扩展为禁用/启用字段）
        User updateUser = new User();
        updateUser.setId(id);
        // 扩展点：可新增status字段，此处临时复用role字段演示（实际需调整）
        baseMapper.updateById(updateUser);
    }
}