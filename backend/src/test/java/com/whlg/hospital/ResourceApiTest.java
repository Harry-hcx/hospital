package com.whlg.hospital;

import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ResourceApiTest extends BaseApiTest {

    @Test
    void shouldGetHomeIndex() throws Exception {
        mockMvc.perform(get("/api/home/index"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.banners").isArray())
                .andExpect(jsonPath("$.data.banners[0].image").value("/img/banner-1.png"))
                .andExpect(jsonPath("$.data.recommendDoctors[*].id", hasItem(1)));
    }

    @Test
    void shouldListHospitals() throws Exception {
        mockMvc.perform(get("/api/hospitals").param("page", "1").param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(2));
    }

    @Test
    void shouldApplyHospitalFiltersBeforePaging() throws Exception {
        mockMvc.perform(get("/api/hospitals")
                        .param("keyword", "北京")
                        .param("departmentId", "2")
                        .param("page", "1")
                        .param("pageSize", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records.length()").value(1));
    }

    @Test
    void shouldNormalizeHospitalLevelAliases() throws Exception {
        mockMvc.perform(get("/api/hospitals").param("level", "三级甲等"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.records[0].level").value("三级甲等"));
    }

    @Test
    void shouldGetHospitalDetail() throws Exception {
        mockMvc.perform(get("/api/hospitals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("北京第一人民医院"));
    }

    @Test
    void shouldGetHospitalDepartments() throws Exception {
        mockMvc.perform(get("/api/hospitals/1/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..name", hasItem("心血管内科")));
    }

    @Test
    void shouldGetHospitalDoctors() throws Exception {
        mockMvc.perform(get("/api/hospitals/1/doctors").param("page", "1").param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[*].hospitalId", hasItem(1)));
    }

    @Test
    void shouldFilterHospitalDoctorsByPrimaryDepartment() throws Exception {
        mockMvc.perform(get("/api/hospitals/1/doctors")
                        .param("departmentId", "1")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].departmentId").value(2));
    }

    @Test
    void shouldGetDepartmentTree() throws Exception {
        mockMvc.perform(get("/api/departments/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..name", hasItem("内科")));
    }

    @Test
    void shouldListDoctors() throws Exception {
        mockMvc.perform(get("/api/doctors").param("departmentId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[*].departmentId", hasItem(2)));
    }

    @Test
    void shouldIncludeSecondLevelDoctorsWhenFilteringPrimaryDepartment() throws Exception {
        mockMvc.perform(get("/api/doctors").param("departmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(2));
    }

    @Test
    void shouldGetDoctorDetail() throws Exception {
        mockMvc.perform(get("/api/doctors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hospitalName").value("北京第一人民医院"));
    }

    @Test
    void shouldGetDoctorSchedules() throws Exception {
        mockMvc.perform(get("/api/doctors/1/schedules").param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].timeSlot").isString())
                .andExpect(jsonPath("$.data[0].isAvailable").value(true))
                .andExpect(jsonPath("$.data[0].remainCount").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    void shouldTreatBlankScheduleStartDateAsToday() throws Exception {
        mockMvc.perform(get("/api/doctors/1/schedules")
                        .param("startDate", "")
                        .param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void shouldGetDoctorReviews() throws Exception {
        mockMvc.perform(get("/api/doctors/1/reviews").param("page", "1").param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[*].rating", hasItem(5)));
    }

    @Test
    void shouldListDiseases() throws Exception {
        mockMvc.perform(get("/api/diseases").param("keyword", "高血压"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[*].name", hasItem("高血压")));
    }

    @Test
    void shouldGetDiseaseDetail() throws Exception {
        mockMvc.perform(get("/api/diseases/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recommendDoctors").isArray());
    }

    @Test
    void shouldListArticles() throws Exception {
        mockMvc.perform(get("/api/articles").param("keyword", "康复"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[*].title", hasItem("膝关节康复指南")));
    }

    @Test
    void shouldGetArticleDetail() throws Exception {
        mockMvc.perform(get("/api/articles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.departmentName").value("心血管内科"))
                .andExpect(jsonPath("$.data.viewCount").value(1201));

        mockMvc.perform(get("/api/articles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.viewCount").value(1202));
    }

    @Test
    void shouldGlobalSearch() throws Exception {
        mockMvc.perform(get("/api/search/global").param("keyword", "高血压"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.diseaseList").isArray())
                .andExpect(jsonPath("$.data.articleList").isArray())
                .andExpect(jsonPath("$.data.counts.disease").isNumber())
                .andExpect(jsonPath("$.data.diseaseCount").isNumber());
    }
}
