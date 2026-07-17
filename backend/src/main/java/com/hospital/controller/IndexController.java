package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.service.DoctorService;
import com.hospital.vo.DoctorVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/index")
public class IndexController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/listTopDoctorVo")
    public Result<List<DoctorVo>> listTopDoctorVo(int limit) {
        List<DoctorVo> doctorVos = doctorService.listTopDoctorVo(limit);
        return Result.ok(doctorVos);
    }
}
