package com.whlg.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.whlg.hospital.entity.Article;
import com.whlg.hospital.entity.Config;
import com.whlg.hospital.entity.Department;
import com.whlg.hospital.entity.Disease;
import com.whlg.hospital.entity.Doctor;
import com.whlg.hospital.entity.Hospital;
import com.whlg.hospital.entity.HospitalDepartment;
import com.whlg.hospital.entity.Review;
import com.whlg.hospital.entity.Schedule;
import com.whlg.hospital.mapper.ArticleMapper;
import com.whlg.hospital.mapper.ConfigMapper;
import com.whlg.hospital.mapper.DepartmentMapper;
import com.whlg.hospital.mapper.DiseaseMapper;
import com.whlg.hospital.mapper.DoctorMapper;
import com.whlg.hospital.mapper.HospitalDepartmentMapper;
import com.whlg.hospital.mapper.HospitalMapper;
import com.whlg.hospital.mapper.ReviewMapper;
import com.whlg.hospital.mapper.ScheduleMapper;
import com.whlg.hospital.service.MedicalResourceService;
import com.whlg.hospital.support.ApiException;
import com.whlg.hospital.util.StatusCode;
import com.whlg.hospital.vo.PageResult;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MedicalResourceServiceImpl extends ServiceSupport implements MedicalResourceService {

    private final HospitalMapper hospitalMapper;
    private final DepartmentMapper departmentMapper;
    private final HospitalDepartmentMapper hospitalDepartmentMapper;
    private final DoctorMapper doctorMapper;
    private final ScheduleMapper scheduleMapper;
    private final ReviewMapper reviewMapper;
    private final DiseaseMapper diseaseMapper;
    private final ArticleMapper articleMapper;
    private final ConfigMapper configMapper;

    public MedicalResourceServiceImpl(HospitalMapper hospitalMapper,
                                      DepartmentMapper departmentMapper,
                                      HospitalDepartmentMapper hospitalDepartmentMapper,
                                      DoctorMapper doctorMapper,
                                      ScheduleMapper scheduleMapper,
                                      ReviewMapper reviewMapper,
                                      DiseaseMapper diseaseMapper,
                                      ArticleMapper articleMapper,
                                      ConfigMapper configMapper) {
        this.hospitalMapper = hospitalMapper;
        this.departmentMapper = departmentMapper;
        this.hospitalDepartmentMapper = hospitalDepartmentMapper;
        this.doctorMapper = doctorMapper;
        this.scheduleMapper = scheduleMapper;
        this.reviewMapper = reviewMapper;
        this.diseaseMapper = diseaseMapper;
        this.articleMapper = articleMapper;
        this.configMapper = configMapper;
    }

    @Override
    public Map<String, Object> homeIndex() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("banners", loadBanners());
        result.put("recommendHospitals", hospitalMapper.selectList(new LambdaQueryWrapper<Hospital>().last("limit 3")).stream().map(this::hospitalSummary).collect(Collectors.toList()));
        result.put("recommendDoctors", doctorMapper.selectList(new LambdaQueryWrapper<Doctor>().last("limit 3")).stream()
                .map(item -> doctorSummary(item, hospitalMapper.selectById(item.getHospitalId()), departmentMapper.selectById(item.getDepartmentId())))
                .collect(Collectors.toList()));
        result.put("recommendDiseases", diseaseMapper.selectList(new LambdaQueryWrapper<Disease>().last("limit 3")).stream().map(this::diseaseSummary).collect(Collectors.toList()));
        result.put("recommendArticles", articleMapper.selectList(new LambdaQueryWrapper<Article>().last("limit 3")).stream().map(this::articleSummary).collect(Collectors.toList()));
        return result;
    }

    @Override
    public PageResult<Map<String, Object>> listHospitals(Integer page, Integer pageSize, Long departmentId, String keyword, String level, String province, String city) {
        List<Hospital> hospitals = hospitalMapper.selectList(new LambdaQueryWrapper<Hospital>()
                .like(keyword != null && !keyword.isEmpty(), Hospital::getName, keyword)
                .eq(level != null && !level.isEmpty(), Hospital::getLevel, level)
                .eq(province != null && !province.isEmpty(), Hospital::getProvince, province)
                .eq(city != null && !city.isEmpty(), Hospital::getCity, city));
        if (departmentId != null) {
            List<Long> hospitalIds = hospitalDepartmentMapper.selectList(new LambdaQueryWrapper<HospitalDepartment>().eq(HospitalDepartment::getDepartmentId, departmentId))
                    .stream().map(HospitalDepartment::getHospitalId).collect(Collectors.toList());
            hospitals = hospitals.stream().filter(item -> hospitalIds.contains(item.getId())).collect(Collectors.toList());
        }
        return paginate(hospitals.stream().map(this::hospitalSummary).collect(Collectors.toList()), page, pageSize);
    }

    @Override
    public Map<String, Object> getHospitalDetail(Long hospitalId) {
        Hospital hospital = hospitalMapper.selectById(hospitalId);
        if (hospital == null) {
            throw new ApiException(StatusCode.NOT_FOUND, "医院不存在");
        }
        Map<String, Object> result = hospitalSummary(hospital);
        result.put("intro", hospital.getIntro());
        return result;
    }

    @Override
    public List<Map<String, Object>> getHospitalDepartments(Long hospitalId) {
        Hospital hospital = hospitalMapper.selectById(hospitalId);
        if (hospital == null) {
            throw new ApiException(StatusCode.NOT_FOUND, "医院不存在");
        }
        List<Long> departmentIds = hospitalDepartmentMapper.selectList(new LambdaQueryWrapper<HospitalDepartment>().eq(HospitalDepartment::getHospitalId, hospitalId))
                .stream().map(HospitalDepartment::getDepartmentId).collect(Collectors.toList());
        if (departmentIds.isEmpty()) {
            return Collections.emptyList();
        }
        return buildDepartmentTree(departmentMapper.selectBatchIds(departmentIds));
    }

    @Override
    public PageResult<Map<String, Object>> getHospitalDoctors(Long hospitalId, Integer page, Integer pageSize) {
        return paginate(doctorMapper.selectList(new LambdaQueryWrapper<Doctor>().eq(Doctor::getHospitalId, hospitalId)).stream()
                .map(item -> doctorSummary(item, hospitalMapper.selectById(item.getHospitalId()), departmentMapper.selectById(item.getDepartmentId())))
                .collect(Collectors.toList()), page, pageSize);
    }

    @Override
    public List<Map<String, Object>> getDepartmentTree() {
        return buildDepartmentTree(departmentMapper.selectList(baseDepartmentQuery()));
    }

    @Override
    public List<Map<String, Object>> getPrimaryDepartments() {
        return departmentMapper.selectList(baseDepartmentQuery()
                        .eq(Department::getParentId, 0L))
                .stream()
                .map(this::departmentSummary)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getDepartmentChildren(Long parentId) {
        Department parent = departmentMapper.selectOne(baseDepartmentQuery().eq(Department::getId, parentId));
        if (parent == null) {
            throw new ApiException(StatusCode.NOT_FOUND, "科室不存在");
        }
        return departmentMapper.selectList(baseDepartmentQuery()
                        .eq(Department::getParentId, parentId))
                .stream()
                .map(this::departmentSummary)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<Map<String, Object>> listDoctors(Integer page, Integer pageSize, Long departmentId, String keyword, Long hospitalId) {
        return paginate(doctorMapper.selectList(new LambdaQueryWrapper<Doctor>()
                        .eq(departmentId != null, Doctor::getDepartmentId, departmentId)
                        .eq(hospitalId != null, Doctor::getHospitalId, hospitalId)
                        .and(keyword != null && !keyword.isEmpty(), wrapper -> wrapper.like(Doctor::getName, keyword).or().like(Doctor::getExpertise, keyword)))
                .stream().map(item -> doctorSummary(item, hospitalMapper.selectById(item.getHospitalId()), departmentMapper.selectById(item.getDepartmentId())))
                .collect(Collectors.toList()), page, pageSize);
    }

    @Override
    public Map<String, Object> getDoctorDetail(Long doctorId) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            throw new ApiException(StatusCode.NOT_FOUND, "医生不存在");
        }
        Map<String, Object> result = doctorSummary(doctor, hospitalMapper.selectById(doctor.getHospitalId()), departmentMapper.selectById(doctor.getDepartmentId()));
        result.put("intro", doctor.getIntro());
        return result;
    }

    @Override
    public List<Map<String, Object>> getDoctorSchedules(Long doctorId, String startDate, Integer days) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            throw new ApiException(StatusCode.NOT_FOUND, "医生不存在");
        }
        LocalDate begin = startDate == null ? LocalDate.now() : LocalDate.parse(startDate);
        int dayCount = days == null || days < 1 ? 7 : days;
        LocalDate end = begin.plusDays(dayCount - 1L);
        return scheduleMapper.selectList(new LambdaQueryWrapper<Schedule>()
                        .eq(Schedule::getDoctorId, doctorId)
                        .ge(Schedule::getScheduleDate, begin)
                        .le(Schedule::getScheduleDate, end)
                        .orderByAsc(Schedule::getScheduleDate))
                .stream().map(item -> {
                    Map<String, Object> result = new LinkedHashMap<String, Object>();
                    result.put("id", item.getId());
                    result.put("scheduleDate", item.getScheduleDate());
                    result.put("weekDay", weekDay(item.getScheduleDate().getDayOfWeek()));
                    result.put("timeSlot", item.getTimeSlot());
                    result.put("remainCount", item.getRemainCount());
                    result.put("totalCount", item.getTotalCount());
                    result.put("registrationPrice", doctor.getRegistrationPrice());
                    result.put("status", item.getStatus());
                    return result;
                }).collect(Collectors.toList());
    }

    @Override
    public PageResult<Map<String, Object>> getDoctorReviews(Long doctorId, Integer page, Integer pageSize) {
        return paginate(reviewMapper.selectList(new LambdaQueryWrapper<Review>().eq(Review::getDoctorId, doctorId).orderByDesc(Review::getCreateTime)).stream()
                .map(item -> {
                    Map<String, Object> result = new LinkedHashMap<String, Object>();
                    result.put("id", item.getId());
                    result.put("userId", item.getUserId());
                    result.put("doctorId", item.getDoctorId());
                    result.put("rating", item.getRating());
                    result.put("content", item.getContent());
                    result.put("createTime", item.getCreateTime());
                    return result;
                }).collect(Collectors.toList()), page, pageSize);
    }

    @Override
    public PageResult<Map<String, Object>> listDiseases(Integer page, Integer pageSize, Long departmentId, String keyword) {
        return paginate(diseaseMapper.selectList(new LambdaQueryWrapper<Disease>()
                        .eq(departmentId != null, Disease::getDepartmentId, departmentId)
                        .and(keyword != null && !keyword.isEmpty(), wrapper -> wrapper.like(Disease::getName, keyword).or().like(Disease::getAlias, keyword)))
                .stream().map(this::diseaseSummary).collect(Collectors.toList()), page, pageSize);
    }

    @Override
    public Map<String, Object> getDiseaseDetail(Long diseaseId) {
        Disease disease = diseaseMapper.selectById(diseaseId);
        if (disease == null) {
            throw new ApiException(StatusCode.NOT_FOUND, "疾病不存在");
        }
        Map<String, Object> result = diseaseSummary(disease);
        result.put("description", disease.getDescription());
        result.put("symptoms", disease.getSymptoms());
        result.put("treatment", disease.getTreatment());
        result.put("treatmentPeriod", disease.getTreatmentPeriod());
        result.put("cureRate", disease.getCureRate());
        result.put("examinations", disease.getExaminations());
        result.put("departmentId", disease.getDepartmentId());
        result.put("recommendDoctors", doctorMapper.selectList(new LambdaQueryWrapper<Doctor>().eq(Doctor::getDepartmentId, disease.getDepartmentId())).stream()
                .map(item -> doctorSummary(item, hospitalMapper.selectById(item.getHospitalId()), departmentMapper.selectById(item.getDepartmentId())))
                .collect(Collectors.toList()));
        result.put("recommendArticles", articleMapper.selectList(new LambdaQueryWrapper<Article>().eq(Article::getDepartmentId, disease.getDepartmentId())).stream().map(this::articleSummary).collect(Collectors.toList()));
        return result;
    }

    @Override
    public PageResult<Map<String, Object>> listArticles(Integer page, Integer pageSize, Long departmentId, String keyword) {
        return paginate(articleMapper.selectList(new LambdaQueryWrapper<Article>()
                        .eq(departmentId != null, Article::getDepartmentId, departmentId)
                        .and(keyword != null && !keyword.isEmpty(), wrapper -> wrapper.like(Article::getTitle, keyword).or().like(Article::getSummary, keyword)))
                .stream().map(this::articleSummary).collect(Collectors.toList()), page, pageSize);
    }

    @Override
    public Map<String, Object> getArticleDetail(Long articleId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new ApiException(StatusCode.NOT_FOUND, "文章不存在");
        }
        Map<String, Object> result = articleSummary(article);
        result.put("content", article.getContent());
        Department department = departmentMapper.selectById(article.getDepartmentId());
        result.put("departmentName", department == null ? null : department.getName());
        return result;
    }

    @Override
    public Map<String, Object> globalSearch(String keyword, String type, Integer page, Integer pageSize) {
        String safeKeyword = keyword == null ? "" : keyword;
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("hospitalList", listHospitals(page, pageSize, null, safeKeyword, null, null, null).getRecords());
        result.put("doctorList", listDoctors(page, pageSize, null, safeKeyword, null).getRecords());
        result.put("diseaseList", listDiseases(page, pageSize, null, safeKeyword).getRecords());
        result.put("articleList", listArticles(page, pageSize, null, safeKeyword).getRecords());
        if (type != null) {
            Map<String, Object> filtered = new LinkedHashMap<String, Object>();
            filtered.put("hospitalList", "hospital".equals(type) ? result.get("hospitalList") : Collections.emptyList());
            filtered.put("doctorList", "doctor".equals(type) ? result.get("doctorList") : Collections.emptyList());
            filtered.put("diseaseList", "disease".equals(type) ? result.get("diseaseList") : Collections.emptyList());
            filtered.put("articleList", "article".equals(type) ? result.get("articleList") : Collections.emptyList());
            return filtered;
        }
        return result;
    }

    private List<Map<String, Object>> buildDepartmentTree(List<Department> departmentList) {
        List<Department> parentDepartments = departmentList.stream().filter(item -> item.getParentId() == 0L).collect(Collectors.toList());
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (Department parent : parentDepartments) {
            Map<String, Object> parentNode = departmentSummary(parent);
            List<Map<String, Object>> children = departmentList.stream()
                    .filter(item -> parent.getId().equals(item.getParentId()))
                    .map(this::departmentSummary)
                    .collect(Collectors.toList());
            parentNode.put("children", children);
            result.add(parentNode);
        }
        return result;
    }

    private LambdaQueryWrapper<Department> baseDepartmentQuery() {
        return new LambdaQueryWrapper<Department>()
                .eq(Department::getStatus, 1)
                .orderByAsc(Department::getSortOrder, Department::getId);
    }

    private Map<String, Object> departmentSummary(Department department) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("id", department.getId());
        result.put("name", department.getName());
        result.put("description", department.getDescription());
        result.put("parentId", department.getParentId());
        result.put("sortOrder", department.getSortOrder());
        return result;
    }

    private Map<String, Object> diseaseSummary(Disease disease) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("id", disease.getId());
        result.put("name", disease.getName());
        result.put("alias", disease.getAlias());
        result.put("location", disease.getLocation());
        result.put("departmentId", disease.getDepartmentId());
        result.put("followCount", disease.getFollowCount());
        return result;
    }

    private Map<String, Object> articleSummary(Article article) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("id", article.getId());
        result.put("title", article.getTitle());
        result.put("summary", article.getSummary());
        result.put("image", article.getImage());
        result.put("author", article.getAuthor());
        result.put("publishTime", article.getPublishTime());
        return result;
    }

    private List<Map<String, Object>> loadBanners() {
        Config config = configMapper.selectOne(new LambdaQueryWrapper<Config>().eq(Config::getConfigKey, "homeBanners"));
        if (config == null || config.getConfigValue() == null) {
            Map<String, Object> banner = new LinkedHashMap<String, Object>();

            return Collections.singletonList(banner);
        }
        Map<String, Object> banner = new LinkedHashMap<String, Object>();
        
        return Collections.singletonList(banner);
    }

    private String weekDay(DayOfWeek dayOfWeek) {
        return dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.CHINA);
    }
}