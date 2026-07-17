package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 电话咨询订单表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_consult")
public class Consult {
    
    /**
     * 咨询ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 订单号
     */
    private String orderNo;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 医生ID
     */
    private Long doctorId;
    
    /**
     * 咨询人姓名
     */
    private String patientName;
    
    /**
     * 咨询人电话
     */
    private String patientPhone;
    
    /**
     * 病情描述
     */
    private String diseaseDesc;
    
    /**
     * 预约咨询时间
     */
    private LocalDateTime appointmentTime;
    
    /**
     * 咨询时长(分钟)
     */
    private Integer duration;
    
    /**
     * 咨询费用
     */
    private BigDecimal amount;
    
    /**
     * 状态: 1待支付 2已支付 3咨询中 4已完成 5已取消
     */
    private Integer status;
    
    /**
     * 支付时间
     */
    private LocalDateTime payTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
