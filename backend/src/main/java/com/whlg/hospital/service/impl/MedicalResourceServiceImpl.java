package com.whlg.hospital.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import java.util.Arrays;
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
    private final ObjectMapper objectMapper;

    public MedicalResourceServiceImpl(HospitalMapper hospitalMapper,
                                      DepartmentMapper departmentMapper,
                                      HospitalDepartmentMapper hospitalDepartmentMapper,
                                      DoctorMapper doctorMapper,
                                      ScheduleMapper scheduleMapper,
                                      ReviewMapper reviewMapper,
                                      DiseaseMapper diseaseMapper,
                                      ArticleMapper articleMapper,
                                      ConfigMapper configMapper,
                                      ObjectMapper objectMapper) {
        this.hospitalMapper = hospitalMapper;
        this.departmentMapper = departmentMapper;
        this.hospitalDepartmentMapper = hospitalDepartmentMapper;
        this.doctorMapper = doctorMapper;
        this.scheduleMapper = scheduleMapper;
        this.reviewMapper = reviewMapper;
        this.diseaseMapper = diseaseMapper;
        this.articleMapper = articleMapper;
        this.configMapper = configMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, Object> homeIndex() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("banners", loadBanners());
        result.put("recommendHospitals", hospitalMapper.selectList(new LambdaQueryWrapper<Hospital>()
                .eq(Hospital::getStatus, 1).orderByAsc(Hospital::getId).last("limit 3"))
                .stream().map(this::hospitalResourceSummary).collect(Collectors.toList()));
        result.put("recommendDoctors", doctorMapper.selectList(new LambdaQueryWrapper<Doctor>()
                        .eq(Doctor::getStatus, 1).orderByAsc(Doctor::getId).last("limit 3")).stream()
                .map(item -> doctorSummary(item, hospitalMapper.selectById(item.getHospitalId()), departmentMapper.selectById(item.getDepartmentId())))
                .collect(Collectors.toList()));
        result.put("recommendDiseases", diseaseMapper.selectList(new LambdaQueryWrapper<Disease>()
                .orderByAsc(Disease::getId).last("limit 3")).stream().map(this::diseaseSummary).collect(Collectors.toList()));
        result.put("recommendArticles", articleMapper.selectList(new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, 1).orderByDesc(Article::getPublishTime).last("limit 3"))
                .stream().map(this::articleSummary).collect(Collectors.toList()));
        return result;
    }

    @Override
    public PageResult<Map<String, Object>> listHospitals(Integer page, Integer pageSize, Long departmentId, String keyword, String level, String province, String city) {
        String safeKeyword = normalize(keyword);
        String normalizedLevel = normalizeLevel(level);
        LambdaQueryWrapper<Hospital> query = new LambdaQueryWrapper<Hospital>()
                .eq(Hospital::getStatus, 1)
                .eq(normalize(province) != null, Hospital::getProvince, normalize(province))
                .eq(normalize(city) != null, Hospital::getCity, normalize(city))
                .and(safeKeyword != null, wrapper -> wrapper.like(Hospital::getName, safeKeyword)
                        .or().like(Hospital::getAddress, safeKeyword)
                        .or().like(Hospital::getProvince, safeKeyword)
                        .or().like(Hospital::getCity, safeKeyword));
        if (normalizedLevel != null) {
            query.in(Hospital::getLevel, levelValues(normalizedLevel));
        }
        if (departmentId != null) {
            List<Long> departmentIds = departmentScopeIds(departmentId);
            List<Long> hospitalIds = hospitalDepartmentMapper.selectList(new LambdaQueryWrapper<HospitalDepartment>().in(HospitalDepartment::getDepartmentId, departmentIds))
                    .stream().map(HospitalDepartment::getHospitalId).collect(Collectors.toList());
            if (hospitalIds.isEmpty()) {
                return emptyPage(page, pageSize);
            }
            query.in(Hospital::getId, hospitalIds);
        }
        return mapPage(hospitalMapper.selectPage(new Page<Hospital>(safePage(page), safePageSize(pageSize)), query), this::hospitalResourceSummary);
    }

    @Override
    public Map<String, Object> getHospitalDetail(Long hospitalId) {
        Hospital hospital = hospitalMapper.selectById(hospitalId);
        if (hospital == null) {
            throw new ApiException(StatusCode.NOT_FOUND, "医院不存在");
        }
        Map<String, Object> result = hospitalResourceSummary(hospital);
        result.put("intro", hospital.getIntro());
        result.put("departments", getHospitalDepartments(hospitalId));
        result.put("doctors", getHospitalDoctors(hospitalId, 1, 10, null).getRecords());
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
    public PageResult<Map<String, Object>> getHospitalDoctors(Long hospitalId, Integer page, Integer pageSize, Long departmentId) {
        if (hospitalMapper.selectById(hospitalId) == null) {
            throw new ApiException(StatusCode.NOT_FOUND, "Hospital not found");
        }
        LambdaQueryWrapper<Doctor> query = new LambdaQueryWrapper<Doctor>()
                .eq(Doctor::getHospitalId, hospitalId)
                .eq(Doctor::getStatus, 1)
                .orderByAsc(Doctor::getId);
        if (departmentId != null) {
            query.in(Doctor::getDepartmentId, departmentScopeIds(departmentId));
        }
        Page<Doctor> doctors = doctorMapper.selectPage(new Page<Doctor>(safePage(page), safePageSize(pageSize)),
                query);
        return mapPage(doctors, item -> doctorSummary(item, hospitalMapper.selectById(item.getHospitalId()),
                departmentMapper.selectById(item.getDepartmentId())));
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
        String safeKeyword = normalize(keyword);
        LambdaQueryWrapper<Doctor> query = new LambdaQueryWrapper<Doctor>().eq(Doctor::getStatus, 1)
                        .eq(hospitalId != null, Doctor::getHospitalId, hospitalId)
                        .and(safeKeyword != null, wrapper -> wrapper.like(Doctor::getName, safeKeyword)
                                .or().like(Doctor::getTitle, safeKeyword).or().like(Doctor::getExpertise, safeKeyword))
                        .orderByAsc(Doctor::getId);
        if (departmentId != null) {
            query.in(Doctor::getDepartmentId, departmentScopeIds(departmentId));
        }
        Page<Doctor> doctors = doctorMapper.selectPage(new Page<Doctor>(safePage(page), safePageSize(pageSize)), query);
        return mapPage(doctors, item -> doctorSummary(item, hospitalMapper.selectById(item.getHospitalId()),
                departmentMapper.selectById(item.getDepartmentId())));
    }

    @Override
    public Map<String, Object> getDoctorDetail(Long doctorId) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            throw new ApiException(StatusCode.NOT_FOUND, "医生不存在");
        }
        Map<String, Object> result = doctorSummary(doctor, hospitalMapper.selectById(doctor.getHospitalId()), departmentMapper.selectById(doctor.getDepartmentId()));
        result.put("intro", doctor.getIntro());
        result.put("schedules", getDoctorSchedules(doctorId, null, 14));
        result.put("reviews", getDoctorReviews(doctorId, 1, 10).getRecords());
        return result;
    }

    @Override
    public List<Map<String, Object>> getDoctorSchedules(Long doctorId, String startDate, Integer days) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            throw new ApiException(StatusCode.NOT_FOUND, "医生不存在");
        }
        LocalDate today = LocalDate.now();
        LocalDate begin = startDate == null || startDate.trim().isEmpty()
                ? today
                : LocalDate.parse(startDate.trim());
        begin = begin.isBefore(today) ? today : begin;
        int dayCount = days == null || days < 1 ? 7 : Math.min(days, 31);
        LocalDate end = begin.plusDays(dayCount - 1L);
        return scheduleMapper.selectList(new LambdaQueryWrapper<Schedule>()
                        .eq(Schedule::getDoctorId, doctorId)
                        .eq(Schedule::getStatus, 1)
                        .gt(Schedule::getRemainCount, 0)
                        .eq(Schedule::getHospitalId, doctor.getHospitalId())
                        .eq(Schedule::getDepartmentId, doctor.getDepartmentId())
                        .ge(Schedule::getScheduleDate, begin)
                        .le(Schedule::getScheduleDate, end)
                        .orderByAsc(Schedule::getScheduleDate).orderByAsc(Schedule::getTimeSlot))
                .stream().map(item -> {
                    Map<String, Object> result = new LinkedHashMap<String, Object>();
                    result.put("id", item.getId());
                    result.put("scheduleDate", item.getScheduleDate());
                    result.put("weekDay", weekDay(item.getScheduleDate().getDayOfWeek()));
                    result.put("timeSlot", item.getTimeSlot());
                    result.put("remainCount", item.getRemainCount());
                    result.put("totalCount", item.getTotalCount());
                    result.put("hospitalId", item.getHospitalId());
                    result.put("departmentId", item.getDepartmentId());
                    result.put("registrationPrice", doctor.getRegistrationPrice());
                    result.put("status", item.getStatus());
                    result.put("isAvailable", item.getRemainCount() != null && item.getRemainCount() > 0
                            && !item.getScheduleDate().isBefore(LocalDate.now()));
                    return result;
                }).collect(Collectors.toList());
    }

    @Override
    public PageResult<Map<String, Object>> getDoctorReviews(Long doctorId, Integer page, Integer pageSize) {
        Page<Review> reviews = reviewMapper.selectPage(new Page<Review>(safePage(page), safePageSize(pageSize)),
                new LambdaQueryWrapper<Review>().eq(Review::getDoctorId, doctorId).orderByDesc(Review::getCreateTime));
        return mapPage(reviews, item -> {
                    Map<String, Object> result = new LinkedHashMap<String, Object>();
                    result.put("id", item.getId());
                    result.put("userId", item.getUserId());
                    result.put("doctorId", item.getDoctorId());
                    result.put("rating", item.getRating());
                    result.put("content", item.getContent());
                    result.put("createTime", item.getCreateTime());
                    return result;
                });
    }

    @Override
    public PageResult<Map<String, Object>> listDiseases(Integer page, Integer pageSize, Long departmentId, String keyword) {
        Page<Disease> diseases = diseaseMapper.selectPage(new Page<Disease>(safePage(page), safePageSize(pageSize)), new LambdaQueryWrapper<Disease>()
                        .in(departmentId != null, Disease::getDepartmentId, departmentScopeIds(departmentId))
                        .and(normalize(keyword) != null, wrapper -> wrapper.like(Disease::getName, normalize(keyword))
                                .or().like(Disease::getAlias, normalize(keyword)).or().like(Disease::getSymptoms, normalize(keyword)))
                        .orderByAsc(Disease::getId));
        return mapPage(diseases, this::diseaseSummary);
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
        Department diseaseDepartment = departmentMapper.selectById(disease.getDepartmentId());
        result.put("departmentName", diseaseDepartment == null ? null : diseaseDepartment.getName());
        result.put("symptom", disease.getSymptoms());
        result.put("recommendDoctors", doctorMapper.selectList(new LambdaQueryWrapper<Doctor>().eq(Doctor::getDepartmentId, disease.getDepartmentId()).eq(Doctor::getStatus, 1)).stream()
                .map(item -> doctorSummary(item, hospitalMapper.selectById(item.getHospitalId()), departmentMapper.selectById(item.getDepartmentId())))
                .collect(Collectors.toList()));
        result.put("recommendArticles", articleMapper.selectList(new LambdaQueryWrapper<Article>().eq(Article::getDepartmentId, disease.getDepartmentId()).eq(Article::getStatus, 1)).stream().map(this::articleSummary).collect(Collectors.toList()));
        return result;
    }

    @Override
    public PageResult<Map<String, Object>> listArticles(Integer page, Integer pageSize, Long departmentId, String keyword) {
        Page<Article> articles = articleMapper.selectPage(new Page<Article>(safePage(page), safePageSize(pageSize)), new LambdaQueryWrapper<Article>()
                        .eq(Article::getStatus, 1)
                        .eq(departmentId != null, Article::getDepartmentId, departmentId)
                        .and(normalize(keyword) != null, wrapper -> wrapper.like(Article::getTitle, normalize(keyword))
                                .or().like(Article::getSummary, normalize(keyword)).or().like(Article::getContent, normalize(keyword)))
                        .orderByDesc(Article::getPublishTime));
        return mapPage(articles, this::articleSummary);
    }

    @Override
    public Map<String, Object> getArticleDetail(Long articleId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new ApiException(StatusCode.NOT_FOUND, "文章不存在");
        }
        articleMapper.update(null, new LambdaUpdateWrapper<Article>()
                .eq(Article::getId, articleId)
                .setSql("views = COALESCE(views, 0) + 1"));
        article = articleMapper.selectById(articleId);
        Map<String, Object> result = articleSummary(article);
        result.put("content", article.getContent());
        Department department = departmentMapper.selectById(article.getDepartmentId());
        result.put("departmentName", department == null ? null : department.getName());
        result.put("department", department == null ? null : departmentSummary(department));
        return result;
    }

    @Override
    public Map<String, Object> globalSearch(String keyword, String type, Integer page, Integer pageSize) {
        String safeKeyword = keyword == null ? null : keyword.trim();
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        PageResult<Map<String, Object>> hospitals = listHospitals(page, pageSize, null, safeKeyword, null, null, null);
        PageResult<Map<String, Object>> doctors = listDoctors(page, pageSize, null, safeKeyword, null);
        PageResult<Map<String, Object>> diseases = listDiseases(page, pageSize, null, safeKeyword);
        PageResult<Map<String, Object>> articles = listArticles(page, pageSize, null, safeKeyword);
        result.put("hospitalList", hospitals.getRecords());
        result.put("doctorList", doctors.getRecords());
        result.put("diseaseList", diseases.getRecords());
        result.put("articleList", articles.getRecords());
        result.put("hospitalCount", hospitals.getTotal());
        result.put("doctorCount", doctors.getTotal());
        result.put("diseaseCount", diseases.getTotal());
        result.put("articleCount", articles.getTotal());
        result.put("hospitalTotal", hospitals.getTotal());
        result.put("doctorTotal", doctors.getTotal());
        result.put("diseaseTotal", diseases.getTotal());
        result.put("articleTotal", articles.getTotal());
        Map<String, Object> counts = new LinkedHashMap<String, Object>();
        counts.put("hospital", hospitals.getTotal());
        counts.put("doctor", doctors.getTotal());
        counts.put("disease", diseases.getTotal());
        counts.put("article", articles.getTotal());
        result.put("counts", counts);
        if (type != null) {
            String normalizedType = type.trim().toLowerCase(Locale.ROOT);
            Map<String, Object> filtered = new LinkedHashMap<String, Object>();
            filtered.put("hospitalList", "hospital".equals(normalizedType) ? result.get("hospitalList") : Collections.emptyList());
            filtered.put("doctorList", "doctor".equals(normalizedType) ? result.get("doctorList") : Collections.emptyList());
            filtered.put("diseaseList", "disease".equals(normalizedType) ? result.get("diseaseList") : Collections.emptyList());
            filtered.put("articleList", "article".equals(normalizedType) ? result.get("articleList") : Collections.emptyList());
            filtered.put("hospitalCount", result.get("hospitalCount"));
            filtered.put("doctorCount", result.get("doctorCount"));
            filtered.put("diseaseCount", result.get("diseaseCount"));
            filtered.put("articleCount", result.get("articleCount"));
            filtered.put("hospitalTotal", result.get("hospitalTotal"));
            filtered.put("doctorTotal", result.get("doctorTotal"));
            filtered.put("diseaseTotal", result.get("diseaseTotal"));
            filtered.put("articleTotal", result.get("articleTotal"));
            filtered.put("counts", counts);
            return filtered;
        }
        return result;
    }

    private String normalize(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private Map<String, Object> hospitalResourceSummary(Hospital hospital) {
        Map<String, Object> result = hospitalSummary(hospital);
        result.put("departmentCount", hospitalDepartmentMapper.selectCount(
                new LambdaQueryWrapper<HospitalDepartment>().eq(HospitalDepartment::getHospitalId, hospital.getId())));
        result.put("doctorCount", doctorMapper.selectCount(new LambdaQueryWrapper<Doctor>()
                .eq(Doctor::getHospitalId, hospital.getId()).eq(Doctor::getStatus, 1)));
        return result;
    }

    /** The API uses full level names while old seed data may use short names. */
    private String normalizeLevel(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        if ("三甲".equals(normalized) || "三级甲".equals(normalized) || "三级甲等".equals(normalized)) {
            return "三级甲等";
        }
        if ("三乙".equals(normalized) || "三级乙".equals(normalized) || "三级乙等".equals(normalized)) {
            return "三级乙等";
        }
        if ("二甲".equals(normalized) || "二级甲".equals(normalized) || "二级甲等".equals(normalized)) {
            return "二级甲等";
        }
        if ("二乙".equals(normalized) || "二级乙".equals(normalized) || "二级乙等".equals(normalized)) {
            return "二级乙等";
        }
        return normalized;
    }

    private List<String> levelValues(String normalizedLevel) {
        if ("三级甲等".equals(normalizedLevel)) {
            return Arrays.asList("三甲", "三级甲等");
        }
        if ("三级乙等".equals(normalizedLevel)) {
            return Arrays.asList("三乙", "三级乙等");
        }
        if ("二级甲等".equals(normalizedLevel)) {
            return Arrays.asList("二甲", "二级甲等");
        }
        if ("二级乙等".equals(normalizedLevel)) {
            return Arrays.asList("二乙", "二级乙等");
        }
        return Collections.singletonList(normalizedLevel);
    }

    private List<Long> departmentScopeIds(Long departmentId) {
        Department department = departmentMapper.selectOne(new LambdaQueryWrapper<Department>()
                .eq(Department::getId, departmentId).eq(Department::getStatus, 1));
        if (department == null) {
            return Collections.singletonList(departmentId);
        }
        if (!Long.valueOf(0L).equals(department.getParentId())) {
            return Collections.singletonList(departmentId);
        }
        List<Long> ids = departmentMapper.selectList(new LambdaQueryWrapper<Department>()
                        .eq(Department::getParentId, departmentId).eq(Department::getStatus, 1))
                .stream().map(Department::getId).collect(Collectors.toList());
        ids.add(departmentId);
        return ids;
    }

    private <T, R> PageResult<R> mapPage(Page<T> page, java.util.function.Function<T, R> mapper) {
        return new PageResult<R>(page.getRecords().stream().map(mapper).collect(Collectors.toList()),
                page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    private <R> PageResult<R> emptyPage(Integer page, Integer pageSize) {
        return new PageResult<R>(Collections.<R>emptyList(), 0L, safePage(page), safePageSize(pageSize));
    }

    private List<Map<String, Object>> buildDepartmentTree(List<Department> departmentList) {
        List<Department> parentDepartments = departmentList.stream().filter(item -> Long.valueOf(0L).equals(item.getParentId())).collect(Collectors.toList());
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
        result.put("createTime", article.getPublishTime());
        result.put("viewCount", article.getViews());
        return result;
    }

    private List<Map<String, Object>> loadBanners() {
        Config config = configMapper.selectOne(new LambdaQueryWrapper<Config>().eq(Config::getConfigKey, "homeBanners"));
        if (config == null || normalize(config.getConfigValue()) == null) {
            return Collections.emptyList();
        }
        try {
            List<Map<String, Object>> banners = objectMapper.readValue(config.getConfigValue(),
                    new TypeReference<List<Map<String, Object>>>() { });
            return banners == null ? Collections.emptyList() : banners;
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    private String weekDay(DayOfWeek dayOfWeek) {
        return dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.CHINA);
    }
}
