package com.whlg.hospital.controller;

import com.whlg.hospital.service.MedicalResourceService;
import com.whlg.hospital.util.R;
import com.whlg.hospital.vo.PageResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hospitals")
public class HospitalController {

    private final MedicalResourceService medicalResourceService;

    public HospitalController(MedicalResourceService medicalResourceService) {
        this.medicalResourceService = medicalResourceService;
    }

    @GetMapping
    public R<PageResult<Map<String, Object>>> list(@RequestParam(required = false) Integer page,
                                                   @RequestParam(required = false) Integer pageSize,
                                                   @RequestParam(required = false) Long departmentId,
                                                   @RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) String level,
                                                   @RequestParam(required = false) String province,
                                                   @RequestParam(required = false) String city) {
        return R.ok(medicalResourceService.listHospitals(page, pageSize, departmentId, keyword, level, province, city));
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable("id") Long id) {
        return R.ok(medicalResourceService.getHospitalDetail(id));
    }

    @GetMapping("/{id}/departments")
    public R<List<Map<String, Object>>> departments(@PathVariable("id") Long id) {
        return R.ok(medicalResourceService.getHospitalDepartments(id));
    }

    @GetMapping("/{id}/doctors")
    public R<PageResult<Map<String, Object>>> doctors(@PathVariable("id") Long id,
                                                      @RequestParam(required = false) Integer page,
                                                      @RequestParam(required = false) Integer pageSize,
                                                      @RequestParam(required = false) Long departmentId) {
        return R.ok(medicalResourceService.getHospitalDoctors(id, page, pageSize, departmentId));
    }
}
