package com.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.Doctor;
import com.hospital.vo.DoctorVo;

import java.util.List;

public interface DoctorMapper extends BaseMapper<Doctor> {
    List<DoctorVo> selectDoctorVoBy(int limit);
}
