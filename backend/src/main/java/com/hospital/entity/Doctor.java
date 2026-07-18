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
 * 医生表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_doctor")
public class Doctor {
    
    /**
     * 医生ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 医生姓名
     */
    private String name;
    
    /**
     * 性别: 1男 2女
     */
    private Integer gender;
    
    /**
     * 职称
     */
    private String title;
    
    /**
     * 所属科室ID
     */
    private Long departmentId;
    
    /**
     * 所属医院ID
     */
    private Long hospitalId;
    
    /**
     * 医生头像
     */
    private String avatar;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 医生简介
     */
    private String intro;
    
    /**
     * 擅长领域
     */
    private String expertise;
    
    /**
     * 接诊次数
     */
    private Integer consultCount;
    
    /**
     * 评分
     */
    private BigDecimal rating;
    
    /**
     * 关注数量
     */
    private Integer followCount;
    
    /**
     * 在线状态: 1在线 2离线
     */
    private Integer onlineStatus;
    
    /**
     * 咨询价格
     */
    private BigDecimal price;
    
    /**
     * 挂号价格
     */
    private BigDecimal registrationPrice;
    
    /**
     * 状态: 1正常 0禁用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
