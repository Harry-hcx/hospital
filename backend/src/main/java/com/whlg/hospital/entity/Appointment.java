package com.whlg.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 挂号订单表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_appointment")
public class Appointment {
    
    /**
     * 订单ID
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
     * 医院ID
     */
    private Long hospitalId;
    
    /**
     * 就诊人姓名
     */
    private String patientName;
    
    /**
     * 就诊人电话
     */
    private String patientPhone;
    
    /**
     * 就诊人身份证号
     */
    private String patientIdCard;
    
    /**
     * 就诊人性别: 1男 2女
     */
    private Integer patientGender;
    
    /**
     * 就诊人年龄
     */
    private Integer patientAge;
    
    /**
     * 预约日期
     */
    private LocalDate appointmentDate;
    
    /**
     * 预约时间段
     */
    private String appointmentTime;
    
    /**
     * 病情描述
     */
    private String diseaseDesc;
    
    /**
     * 挂号费用
     */
    private BigDecimal amount;
    
    /**
     * 状态: 1待支付 2已支付 3已完成 4已取消
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
