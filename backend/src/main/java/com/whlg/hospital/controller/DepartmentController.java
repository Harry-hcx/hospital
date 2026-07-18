package com.whlg.hospital.controller;

import com.whlg.hospital.service.MedicalResourceService;
import com.whlg.hospital.util.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final MedicalResourceService medicalResourceService;

    public DepartmentController(MedicalResourceService medicalResourceService) {
        this.medicalResourceService = medicalResourceService;
    }

    @GetMapping("/tree")
    public R<List<Map<String, Object>>> tree() {
        return R.ok(medicalResourceService.getDepartmentTree());
    }

    @GetMapping("/primary")
    public R<List<Map<String, Object>>> getPrimaryDepartments() {
        return R.ok(medicalResourceService.getPrimaryDepartments());
    }

    @GetMapping("/{parentId}/children")
    public R<List<Map<String, Object>>> getDepartmentChildren(@PathVariable Long parentId) {
        return R.ok(medicalResourceService.getDepartmentChildren(parentId));
    }
}