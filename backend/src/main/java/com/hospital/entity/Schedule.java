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
 * 医生排班表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_schedule")
public class Schedule {

    /**
     * 排班ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 医生ID
     */
    private Long doctorId;

    /**
     * 医院ID
     */
    private Long hospitalId;

    /**
     * 科室ID
     */
    private Long departmentId;

    /**
     * 排班日期
     */
    private LocalDate scheduleDate;

    /**
     * 时间段(08:00-08:30等)
     */
    private String timeSlot;

    /**
     * 总号源
     */
    private Integer totalCount;

    /**
     * 剩余号源
     */
    private Integer remainCount;

    /**
     * 状态: 1可预约 0已约满
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
