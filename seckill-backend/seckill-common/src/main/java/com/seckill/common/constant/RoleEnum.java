package com.seckill.common.constant;

import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum RoleEnum {
    ADMIN(1, "管理员"),
    USER(2, "普通用户");

    private final Integer code;
    private final String desc;

    RoleEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 根据code获取枚举
    public static RoleEnum getByCode(Integer code) {
        for (RoleEnum role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return null;
    }
}