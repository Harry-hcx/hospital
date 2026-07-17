package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 反馈表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_feedback")
public class Feedback {
    
    /**
     * 反馈ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 反馈类型: 1系统问题 2服务问题 3医生问题 4其他问题
     */
    private Integer feedbackType;
    
    /**
     * 反馈内容
     */
    private String content;
    
    /**
     * 反馈图片(逗号分隔)
     */
    private String images;
    
    /**
     * 状态: 1待处理 2已处理
     */
    private Integer status;
    
    /**
     * 回复内容
     */
    private String replyContent;
    
    /**
     * 回复时间
     */
    private LocalDateTime replyTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
