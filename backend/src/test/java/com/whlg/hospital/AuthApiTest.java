package com.whlg.hospital;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthApiTest extends BaseApiTest {

    @Test
    void shouldRegister() throws Exception {
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("username", "api-user");
        request.put("phone", "13900139000");
        request.put("password", "123456");
        request.put("gender", 2);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").isNumber());
    }

    @Test
    void shouldLogin() throws Exception {
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("phone", "13800138000");
        request.put("password", "123456");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isString())
                .andExpect(jsonPath("$.data.userInfo.phone").value("13800138000"))
                .andExpect(jsonPath("$.data.userInfo.gender").value(1));
    }

    @Test
    void shouldGetCurrentUser() throws Exception {
        mockMvc.perform(get("/api/auth/me").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.realName").value("张三"))
                .andExpect(jsonPath("$.data.userInfo").doesNotExist());
    }

    @Test
    void shouldChangePassword() throws Exception {
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("oldPassword", "123456");
        request.put("newPassword", "abc12345");

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldLogout() throws Exception {
        mockMvc.perform(post("/api/auth/logout").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
