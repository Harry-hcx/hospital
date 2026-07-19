package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评价表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_review")
public class Review {
    
    /**
     * 评价ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 订单类型: 1挂号 2咨询
     */
    private Integer orderType;
    
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 医生ID
     */
    private Long doctorId;
    
    /**
     * 评分(1-5)
     */
    private Integer rating;
    
    /**
     * 评价内容
     */
    private String content;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
