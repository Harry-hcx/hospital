package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 疾病表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_disease")
public class Disease {
    
    /**
     * 疾病ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 所属科室ID（关联二级科室）
     */
    private Long departmentId;
    
    /**
     * 疾病名称
     */
    private String name;
    
    /**
     * 疾病描述
     */
    private String description;
    
    /**
     * 别名
     */
    private String alias;
    
    /**
     * 发病部位
     */
    private String location;
    
    /**
     * 治疗方法
     */
    private String treatment;
    
    /**
     * 常见症状
     */
    private String symptoms;
    
    /**
     * 治疗周期
     */
    private String treatmentPeriod;
    
    /**
     * 治愈率
     */
    private String cureRate;
    
    /**
     * 临床检查
     */
    private String examinations;
    
    /**
     * 关注数量
     */
    private Integer followCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
