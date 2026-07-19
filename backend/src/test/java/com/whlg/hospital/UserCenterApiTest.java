package com.whlg.hospital;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserCenterApiTest extends BaseApiTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldGetProfile() throws Exception {
        mockMvc.perform(get("/api/user/profile").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.phone").value("13800138000"))
                .andExpect(jsonPath("$.data.realName").value("张三"))
                .andExpect(jsonPath("$.data.gender").value(1))
                .andExpect(jsonPath("$.data.name").doesNotExist());
    }

    @Test
    void shouldUpdateProfile() throws Exception {
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("realName", "张三丰");
        request.put("gender", 1);
        request.put("birthday", "1991-01-01");
        request.put("email", "new@example.com");
        request.put("avatar", "/avatar/new.png");

        mockMvc.perform(put("/api/user/profile")
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldListFamilyMembers() throws Exception {
        mockMvc.perform(get("/api/family-members").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].name", hasItem("张三")));
    }

    @Test
    void shouldCreateFamilyMember() throws Exception {
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("name", "小张");
        request.put("phone", "13800138111");
        request.put("relation", "子女");
        request.put("gender", 1);
        request.put("birthday", "2015-05-01");
        request.put("idCard", "110101201505010033");
        request.put("isDefault", 0);

        mockMvc.perform(post("/api/family-members")
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber());
    }

    @Test
    void shouldKeepOnlyOneDefaultFamilyMember() throws Exception {
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("name", "新的默认就诊人");
        request.put("relation", "其他");
        request.put("isDefault", 1);

        mockMvc.perform(post("/api/family-members")
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/family-members").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.isDefault == 1)]", hasSize(1)));
    }

    @Test
    void shouldAllowFamilyMemberWithoutBirthday() throws Exception {
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("name", "未填生日");
        request.put("relation", "其他");
        request.put("isDefault", 0);

        mockMvc.perform(post("/api/family-members")
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber());
    }

    @Test
    void shouldUpdateFamilyMember() throws Exception {
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("name", "张三-更新");
        request.put("phone", "13800138000");
        request.put("relation", "本人");
        request.put("gender", 1);
        request.put("birthday", "1990-01-01");
        request.put("idCard", "110101199001010011");
        request.put("isDefault", 1);

        mockMvc.perform(put("/api/family-members/1")
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldDeleteFamilyMember() throws Exception {
        mockMvc.perform(delete("/api/family-members/2").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldListMyReviews() throws Exception {
        mockMvc.perform(get("/api/reviews/my").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[*].doctorName", hasItem("李主任")));
    }

    @Test
    void shouldCreateReview() throws Exception {
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("orderType", 2);
        request.put("orderId", 1);
        request.put("doctorId", 1);
        request.put("content", "服务很好");
        request.put("rating", 5);

        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber());

        mockMvc.perform(get("/api/doctors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rating").value(5.0));
    }

    @Test
    void shouldRejectDuplicateReview() throws Exception {
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("orderType", 1);
        request.put("orderId", 2);
        request.put("doctorId", 1);
        request.put("content", "重复评价");
        request.put("rating", 3);

        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldRejectReviewBeforeAppointmentCompleted() throws Exception {
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("orderType", 1);
        request.put("orderId", 1);
        request.put("doctorId", 1);
        request.put("content", "尚未就诊");
        request.put("rating", 5);

        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldListMessages() throws Exception {
        mockMvc.perform(get("/api/messages").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    void shouldMarkMessageRead() throws Exception {
        mockMvc.perform(post("/api/messages/1/read").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldGetUnreadCount() throws Exception {
        mockMvc.perform(get("/api/messages/unread-count").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").isNumber());
    }

    @Test
    void shouldListFeedbacks() throws Exception {
        mockMvc.perform(get("/api/feedbacks/my").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[*].type", hasItem(1)));
    }

    @Test
    void shouldCreateFeedback() throws Exception {
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("feedbackType", 2);
        request.put("content", "客服响应慢");
        request.put("images", Arrays.asList("/img/x.png", "/img/y.png"));

        mockMvc.perform(post("/api/feedbacks")
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber());
    }

    @Test
    void shouldListFollows() throws Exception {
        mockMvc.perform(get("/api/follow/my").header("Authorization", auth()).param("type", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[*].type", hasItem(1)));
    }

    @Test
    void shouldCreateFollow() throws Exception {
        mockMvc.perform(post("/api/follow/doctor/2").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber());
    }

    @Test
    void shouldHideAndAllowDeletingStaleFollow() throws Exception {
        jdbcTemplate.update("update t_follow set follow_id = ? where id = ?", 999L, 1L);

        mockMvc.perform(get("/api/follow/my").header("Authorization", auth()).param("type", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));

        mockMvc.perform(delete("/api/follow/1/999").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldRejectFollowForMissingTarget() throws Exception {
        mockMvc.perform(post("/api/follow/doctor/9999").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldRejectInvalidFeedback() throws Exception {
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("feedbackType", 9);
        request.put("content", "");

        mockMvc.perform(post("/api/feedbacks")
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldDeleteFollow() throws Exception {
        mockMvc.perform(delete("/api/follow/3/1").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
