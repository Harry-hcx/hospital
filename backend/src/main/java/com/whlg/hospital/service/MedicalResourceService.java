package com.whlg.hospital.service;

import com.whlg.hospital.vo.PageResult;

import java.util.List;
import java.util.Map;

public interface MedicalResourceService {

    Map<String, Object> homeIndex();

    PageResult<Map<String, Object>> listHospitals(Integer page, Integer pageSize, Long departmentId, String keyword, String level, String province, String city);

    Map<String, Object> getHospitalDetail(Long hospitalId);

    List<Map<String, Object>> getHospitalDepartments(Long hospitalId);

    PageResult<Map<String, Object>> getHospitalDoctors(Long hospitalId, Integer page, Integer pageSize);

    List<Map<String, Object>> getDepartmentTree();

    List<Map<String, Object>> getPrimaryDepartments();

    List<Map<String, Object>> getDepartmentChildren(Long parentId);

    PageResult<Map<String, Object>> listDoctors(Integer page, Integer pageSize, Long departmentId, String keyword, Long hospitalId);

    Map<String, Object> getDoctorDetail(Long doctorId);

    List<Map<String, Object>> getDoctorSchedules(Long doctorId, String startDate, Integer days);

    PageResult<Map<String, Object>> getDoctorReviews(Long doctorId, Integer page, Integer pageSize);

    PageResult<Map<String, Object>> listDiseases(Integer page, Integer pageSize, Long departmentId, String keyword);

    Map<String, Object> getDiseaseDetail(Long diseaseId);

    PageResult<Map<String, Object>> listArticles(Integer page, Integer pageSize, Long departmentId, String keyword);

    Map<String, Object> getArticleDetail(Long articleId);

    Map<String, Object> globalSearch(String keyword, String type, Integer page, Integer pageSize);
}