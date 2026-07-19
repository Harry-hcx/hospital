package com.whlg.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 医院表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_hospital")
public class Hospital {
    
    /**
     * 医院ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 医院名称
     */
    private String name;
    
    /**
     * 医院等级
     */
    private String level;
    
    /**
     * 医院地址
     */
    private String address;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 医院简介
     */
    private String intro;
    
    /**
     * 医院图片
     */
    private String image;
    
    /**
     * 省份
     */
    private String province;
    
    /**
     * 城市
     */
    private String city;
    
    /**
     * 科室数量
     */
    private Integer departmentCount;
    
    /**
     * 医生数量
     */
    private Integer doctorCount;
    
    /**
     * 关注数量
     */
    private Integer followCount;
    
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
