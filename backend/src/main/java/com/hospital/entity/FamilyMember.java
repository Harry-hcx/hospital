package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 就诊成员表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_family_member")
public class FamilyMember {
    
    /**
     * 成员ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID(户主)
     */
    private Long userId;
    
    /**
     * 姓名
     */
    private String name;
    
    /**
     * 性别: 1男 2女
     */
    private Integer gender;
    
    /**
     * 生日
     */
    private LocalDate birthday;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 身份证号
     */
    private String idCard;
    
    /**
     * 与本人关系 1:本人 2:配偶 3:父母 4:子女 5:兄弟姐妹 6:其他
     */
    private String relation;
    
    /**
     * 是否默认: 1是 0否
     */
    private Integer isDefault;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
