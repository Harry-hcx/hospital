package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 科室表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_department")
public class Department {
    
    /**
     * 科室ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 科室名称
     */
    private String name;
    
    /**
     * 科室描述
     */
    private String description;
    
    /**
     * 父级科室ID
     */
    private Long parentId;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    /**
     * 状态: 1正常 0禁用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
