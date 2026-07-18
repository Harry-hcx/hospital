package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 健康科普文章表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_article")
public class Article {
    
    /**
     * 文章ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 文章标题
     */
    private String title;
    
    /**
     * 文章摘要
     */
    private String summary;
    
    /**
     * 文章内容
     */
    private String content;
    
    /**
     * 所属科室ID
     */
    private Long departmentId;
    
    /**
     * 作者
     */
    private String author;
    
    /**
     * 封面图片
     */
    private String image;
    
    /**
     * 阅读量
     */
    private Integer views;
    
    /**
     * 状态: 1已发布 0草稿
     */
    private Integer status;
    
    /**
     * 发布时间
     */
    private LocalDateTime publishTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
