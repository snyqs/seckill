package com.seckill.common.entity;

import lombok.Data;

import java.util.List;

/**
 * 通用分页返回实体
 */
@Data
public class PageInfo<T> {
    /**
     * 当前页码
     */
    private Integer pageNum;
    /**
     * 每页条数
     */
    private Integer pageSize;
    /**
     * 总条数
     */
    private Long total;
    /**
     * 总页数
     */
    private Integer pages;
    /**
     * 数据列表
     */
    private List<T> list;
}