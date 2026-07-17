package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 医院-科室关联表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_hospital_department")
public class HospitalDepartment {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 医院ID
     */
    private Long hospitalId;
    
    /**
     * 科室ID
     */
    private Long departmentId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
