package com.whlg.hospital.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorVo {

    private Long id;
    private String doctorName;
    private String title;
    private String avatar;
    private String hospitalName;
    private String departmentName;

}
