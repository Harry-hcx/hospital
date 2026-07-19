package com.hospital.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.entity.Doctor;
import com.hospital.vo.DoctorVo;

import java.util.List;

public interface DoctorService extends IService<Doctor> {
    List<DoctorVo> listTopDoctorVo(int limit);
}
