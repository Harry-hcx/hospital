package com.whlg.hospital.service.impl;

import com.whlg.hospital.entity.Department;
import com.whlg.hospital.entity.Doctor;
import com.whlg.hospital.entity.Hospital;
import com.whlg.hospital.support.ApiException;
import com.whlg.hospital.support.CurrentUserHolder;
import com.whlg.hospital.util.StatusCode;
import com.whlg.hospital.vo.PageResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

abstract class ServiceSupport {

    protected Integer safePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    protected Integer safePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? 10 : pageSize;
    }

    protected <T> PageResult<T> paginate(List<T> records, Integer page, Integer pageSize) {
        int safePage = safePage(page);
        int safePageSize = safePageSize(pageSize);
        int fromIndex = (safePage - 1) * safePageSize;
        if (fromIndex >= records.size()) {
            return new PageResult<T>(Collections.<T>emptyList(), (long) records.size(), safePage, safePageSize);
        }
        int toIndex = Math.min(fromIndex + safePageSize, records.size());
        return new PageResult<T>(new ArrayList<T>(records.subList(fromIndex, toIndex)), (long) records.size(), safePage, safePageSize);
    }

    protected Long requireUserId() {
        Long userId = CurrentUserHolder.get();
        if (userId == null) {
            throw new ApiException(StatusCode.UNAUTHORIZED, "未登录");
        }
        return userId;
    }

    protected void check(boolean expression, String message) {
        if (!expression) {
            throw new ApiException(StatusCode.BAD_REQUEST, message);
        }
    }

    protected Map<String, Object> pageMap(PageResult<Map<String, Object>> pageResult) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("list", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", pageResult.getPage());
        result.put("pageSize", pageResult.getPageSize());
        return result;
    }

    protected Map<String, Object> hospitalSummary(Hospital hospital) {
        Map<String, Object> item = new LinkedHashMap<String, Object>();
        item.put("id", hospital.getId());
        item.put("name", hospital.getName());
        item.put("level", canonicalHospitalLevel(hospital.getLevel()));
        item.put("address", hospital.getAddress());
        item.put("phone", hospital.getPhone());
        item.put("image", hospital.getImage());
        item.put("avatar", hospital.getImage());
        item.put("description", hospital.getIntro());
        item.put("province", hospital.getProvince());
        item.put("city", hospital.getCity());
        item.put("departmentCount", hospital.getDepartmentCount());
        item.put("doctorCount", hospital.getDoctorCount());
        item.put("followCount", hospital.getFollowCount());
        return item;
    }

    private String canonicalHospitalLevel(String level) {
        if ("三甲".equals(level) || "三级甲".equals(level)) return "三级甲等";
        if ("三乙".equals(level) || "三级乙".equals(level)) return "三级乙等";
        if ("二甲".equals(level) || "二级甲".equals(level)) return "二级甲等";
        if ("二乙".equals(level) || "二级乙".equals(level)) return "二级乙等";
        return level;
    }

    protected Map<String, Object> doctorSummary(Doctor doctor, Hospital hospital, Department department) {
        Map<String, Object> item = new LinkedHashMap<String, Object>();
        item.put("id", doctor.getId());
        item.put("name", doctor.getName());
        item.put("title", doctor.getTitle());
        item.put("avatar", doctor.getAvatar());
        item.put("description", doctor.getIntro());
        item.put("hospitalId", doctor.getHospitalId());
        item.put("hospitalName", hospital == null ? null : hospital.getName());
        item.put("departmentId", doctor.getDepartmentId());
        item.put("departmentName", department == null ? null : department.getName());
        item.put("expertise", doctor.getExpertise());
        item.put("consultPrice", doctor.getPrice());
        item.put("consultFee", doctor.getPrice());
        item.put("registrationPrice", doctor.getRegistrationPrice());
        item.put("followCount", doctor.getFollowCount());
        item.put("rating", doctor.getRating());
        item.put("avgRating", doctor.getRating());
        return item;
    }
}
