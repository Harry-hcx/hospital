package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 关注表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_follow")
public class Follow {
    
    /**
     * 关注ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 关注类型: 1医院 2医生 3疾病
     */
    private Integer followType;
    
    /**
     * 关注对象ID
     */
    private Long followId;
    
    /**
     * 关注时间
     */
    private LocalDateTime createTime;
}
