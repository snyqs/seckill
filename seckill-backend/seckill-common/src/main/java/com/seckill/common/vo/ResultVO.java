package com.seckill.common.vo;

import lombok.Data;

/**
 * 统一响应体
 */
@Data
public class ResultVO<T> {
    /**
     * 响应码：200成功/400参数错/401未登录/403无权限/500服务器错
     */
    private Integer code;
    /**
     * 响应信息
     */
    private String msg;
    /**
     * 响应数据
     */
    private T data;

    // 私有化构造函数，强制通过静态方法创建实例，避免泛型推断问题
    private ResultVO(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // 成功响应（无数据）：显式指定泛型为Void，解决推断问题
    public static <T> ResultVO<T> success() {
        return new ResultVO<>(200, "success", null);
    }

    // 成功响应（有数据）
    public static <T> ResultVO<T> success(T data) {
        return new ResultVO<>(200, "success", data);
    }

    // 失败响应
    public static <T> ResultVO<T> fail(Integer code, String msg) {
        return new ResultVO<>(code, msg, null);
    }

    // 重载失败响应（无code，默认500）
    public static <T> ResultVO<T> fail(String msg) {
        return new ResultVO<>(500, msg, null);
    }
}